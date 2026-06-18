package com.example.kpick.community.service;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityCommentLike;
import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.domain.CommunityPostLike;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.domain.CommunityPostView;
import com.example.kpick.community.dto.req.CreateCommunityCommentRequest;
import com.example.kpick.community.dto.req.CreateCommunityPostRequest;
import com.example.kpick.community.dto.req.ToggleCommunityLikeRequest;
import com.example.kpick.community.dto.req.UpdateCommunityPostRequest;
import com.example.kpick.community.dto.res.CommunityCommentResponse;
import com.example.kpick.community.dto.res.CommunityLikeToggleResponse;
import com.example.kpick.community.dto.res.CommunityPostResponse;
import com.example.kpick.community.repository.CommunityCommentLikeRepository;
import com.example.kpick.community.repository.CommunityCommentRepository;
import com.example.kpick.community.repository.CommunityPostLikeRepository;
import com.example.kpick.community.repository.CommunityPostViewRepository;
import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.thread.dto.req.CreateThreadRequest;
import com.example.kpick.community.thread.dto.req.UpdateThreadRequest;
import com.example.kpick.community.thread.dto.res.ThreadDetailsResponse;
import com.example.kpick.community.thread.dto.res.ThreadResponse;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.domain.UserVoteOption;
import com.example.kpick.community.uservote.domain.UserVoteSelection;
import com.example.kpick.community.uservote.dto.req.CreateUserVoteRequest;
import com.example.kpick.community.uservote.dto.req.SelectUserVoteOptionRequest;
import com.example.kpick.community.uservote.dto.req.UpdateUserVoteRequest;
import com.example.kpick.community.uservote.dto.res.UserVoteDetailsResponse;
import com.example.kpick.community.uservote.dto.res.UserVoteResponse;
import com.example.kpick.community.uservote.repository.UserVoteOptionRepository;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import com.example.kpick.community.uservote.repository.UserVoteSelectionRepository;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.notification.domain.PushNotificationType;
import com.example.kpick.notification.service.PushNotificationService;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private static final int POST_CREATE_ACTIVITY_POINT = 30;
    private static final int COMMENT_CREATE_ACTIVITY_POINT = 10;

    private final ThreadRepository threadRepository;
    private final UserVoteRepository userVoteRepository;
    private final UserVoteOptionRepository userVoteOptionRepository;
    private final UserVoteSelectionRepository userVoteSelectionRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostViewRepository communityPostViewRepository;
    private final CommunityCommentLikeRepository communityCommentLikeRepository;
    private final ProgramRepository programRepository;
    private final ProfileRepository profileRepository;
    private final MissionRepository missionRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getCommunityPosts() {
        return getCommunityPosts(null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getCommunityPosts(CommunityPostType postType, Genre genre, Long programId, Long profileId) {
        List<CommunityPostResponse> posts = postType == null
                ? getAllCommunityPosts(profileId)
                : getCommunityPostsByType(postType, profileId);
        return filterCommunityPosts(posts, genre, programId);
    }

    private List<CommunityPostResponse> getAllCommunityPosts(Long profileId) {
        return Stream.concat(
                        getCommunityPostsByType(CommunityPostType.THREAD, profileId).stream(),
                        getCommunityPostsByType(CommunityPostType.USER_VOTE, profileId).stream()
                )
                .sorted(Comparator.comparing(CommunityPostResponse::getCreatedAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getCommunityPostsByType(CommunityPostType postType) {
        return getCommunityPostsByType(postType, null);
    }

    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getCommunityPostsByType(CommunityPostType postType, Long profileId) {
        return switch (postType) {
            case THREAD -> threadRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(CommunityPost::isVisible)
                    .map(this::toThreadPostResponse)
                    .toList();
            case USER_VOTE -> userVoteRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(CommunityPost::isVisible)
                    .map(userVote -> toUserVotePostResponse(userVote, profileId))
                    .toList();
        };
    }

    private List<CommunityPostResponse> filterCommunityPosts(List<CommunityPostResponse> posts, Genre genre, Long programId) {
        if (genre == null && programId == null) {
            return posts;
        }

        List<Long> programIds = resolveFilterProgramIds(genre, programId);
        if (programIds.isEmpty()) {
            return List.of();
        }

        return posts.stream()
                .filter(post -> programIds.contains(post.getProgramId()))
                .toList();
    }

    private List<Long> resolveFilterProgramIds(Genre genre, Long programId) {
        if (programId != null) {
            Program program = findProgram(programId);
            if (genre != null && program.getGenre() != genre) {
                return List.of();
            }
            return List.of(programId);
        }

        return programRepository.findAll().stream()
                .filter(program -> program.getGenre() == genre)
                .map(Program::getId)
                .toList();
    }

    private CommunityPostResponse toUserVotePostResponse(UserVote userVote, Long profileId) {
        List<UserVoteOption> options = userVoteOptionRepository.findByUserVoteIdOrderByDisplayOrderAsc(userVote.getId());
        Long selectedUserVoteOptionId = profileId == null ? null : userVoteSelectionRepository.findByUserVoteIdAndProfileId(userVote.getId(), profileId)
                .map(selection -> selection.getUserVoteOption().getId())
                .orElse(null);
        return CommunityPostResponse.fromUserVote(userVote, options, selectedUserVoteOptionId, findProfile(userVote.getProfileId()));
    }

    private CommunityPostResponse toThreadPostResponse(Thread thread) {
        Mission mission = thread.getMissionId() == null ? null : findMission(thread.getMissionId());
        Program missionProgram = mission == null ? null : findProgram(mission.getProgramId());
        return CommunityPostResponse.fromThread(thread, mission, missionProgram, findProfile(thread.getProfileId()));
    }

    @Transactional
    public CommunityPostResponse createCommunityPost(CreateCommunityPostRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getPostType() == null) throw new IllegalArgumentException("postType is required.");

        return switch (request.getPostType()) {
            case THREAD -> {
                ThreadResponse thread = createThread(request.toCreateThreadRequest());
                yield toThreadPostResponse(findThread(thread.getThreadId()));
            }
            case USER_VOTE -> {
                UserVoteResponse userVote = createUserVote(request.toCreateUserVoteRequest());
                yield toUserVotePostResponse(findUserVote(userVote.getUserVoteId()), request.getProfileId());
            }
        };
    }

    @Transactional
    public Object getCommunityPostDetails(CommunityPostType postType, Long postId, Long profileId) {
        return switch (postType) {
            case THREAD -> getThreadDetails(postId, profileId);
            case USER_VOTE -> getUserVoteDetails(postId, profileId);
        };
    }

    @Transactional
    public ThreadResponse createThread(CreateThreadRequest request) {
        validateCreateThreadRequest(request);
        Mission mission = request.getMissionId() == null ? null : findMission(request.getMissionId());
        Long resolvedProgramId = mission == null ? request.getProgramId() : mission.getProgramId();
        if (mission != null && request.getProgramId() != null && !mission.getProgramId().equals(request.getProgramId())) {
            throw new IllegalArgumentException("Mission does not belong to this program.");
        }

        Program program = findProgram(resolvedProgramId);
        Profile profile = findProfile(request.getProfileId());
        Thread savedThread = threadRepository.save(Thread.toEntity(request, resolvedProgramId));
        profile.addActivityPoint(POST_CREATE_ACTIVITY_POINT);

        return ThreadResponse.from(savedThread, program);
    }

    @Transactional
    public ThreadDetailsResponse getThreadDetails(Long threadId, Long profileId) {
        Thread thread = findThread(threadId);
        assertVisible(thread);
        increaseViewCountIfFirstView(CommunityPostType.THREAD, threadId, profileId, thread);

        Program program = findProgram(thread.getProgramId());
        Mission mission = thread.getMissionId() == null ? null : findMission(thread.getMissionId());
        return ThreadDetailsResponse.from(thread, program, mission, findProfile(thread.getProfileId()), getComments(CommunityPostType.THREAD, threadId));
    }

    @Transactional
    public CommunityPostResponse updateCommunityPost(
            CommunityPostType postType,
            Long postId,
            Long profileId,
            UpdateCommunityPostRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        assertPostOwner(findCommunityPost(postType, postId), profileId);

        return switch (postType) {
            case THREAD -> {
                ThreadResponse thread = updateThread(postId, request.toUpdateThreadRequest());
                yield toThreadPostResponse(findThread(thread.getThreadId()));
            }
            case USER_VOTE -> {
                UserVoteResponse userVote = updateUserVote(postId, request.toUpdateUserVoteRequest());
                yield toUserVotePostResponse(findUserVote(userVote.getUserVoteId()), profileId);
            }
        };
    }

    @Transactional
    public void deleteCommunityPost(CommunityPostType postType, Long postId, Long profileId) {
        assertPostOwner(findCommunityPost(postType, postId), profileId);
        switch (postType) {
            case THREAD -> deleteThread(postId);
            case USER_VOTE -> deleteUserVote(postId);
        }
    }

    @Transactional
    public ThreadResponse updateThread(Long threadId, UpdateThreadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        validateUpdateThreadRequest(request);
        Thread thread = findThread(threadId);
        Program program = request.getProgramId() == null ? findProgram(thread.getProgramId()) : findProgram(request.getProgramId());
        thread.update(request);

        return ThreadResponse.from(thread, program);
    }

    @Transactional
    public void deleteThread(Long threadId) {
        deleteCommunityRelations(CommunityPostType.THREAD, threadId);
        threadRepository.delete(findThread(threadId));
    }

    @Transactional
    public UserVoteResponse createUserVote(CreateUserVoteRequest request) {
        validateCreateUserVoteRequest(request);
        Profile profile = findProfile(request.getProfileId());
        UserVote userVote = userVoteRepository.save(UserVote.toEntity(request));
        saveUserVoteOptions(userVote, request.getOptions());
        profile.addActivityPoint(POST_CREATE_ACTIVITY_POINT);

        return UserVoteResponse.from(userVote);
    }

    @Transactional
    public UserVoteResponse updateUserVote(Long userVoteId, UpdateUserVoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        validateUpdateUserVoteRequest(request);
        UserVote userVote = findUserVote(userVoteId);
        if (request.getProgramId() != null) {
            findProgram(request.getProgramId());
        }
        userVote.update(request);
        if (request.getOptions() != null) {
            userVoteSelectionRepository.deleteByUserVoteId(userVoteId);
            userVoteOptionRepository.deleteByUserVoteId(userVoteId);
            userVote.resetVoteCount();
            saveUpdatedUserVoteOptions(userVote, request.getOptions());
        }

        return UserVoteResponse.from(userVote);
    }

    @Transactional
    public void deleteUserVote(Long userVoteId) {
        deleteCommunityRelations(CommunityPostType.USER_VOTE, userVoteId);
        userVoteSelectionRepository.deleteByUserVoteId(userVoteId);
        userVoteOptionRepository.deleteByUserVoteId(userVoteId);
        userVoteRepository.delete(findUserVote(userVoteId));
    }

    @Transactional
    public UserVoteDetailsResponse getUserVoteDetails(Long userVoteId, Long profileId) {
        UserVote userVote = findUserVote(userVoteId);
        assertVisible(userVote);
        increaseViewCountIfFirstView(CommunityPostType.USER_VOTE, userVoteId, profileId, userVote);
        return toUserVoteDetails(userVoteId, userVote, profileId);
    }

    @Transactional(readOnly = true)
    public UserVoteDetailsResponse getUserVoteResult(Long userVoteId, Long profileId) {
        UserVote userVote = findUserVote(userVoteId);
        assertVisible(userVote);
        return toUserVoteDetails(userVoteId, userVote, profileId);
    }

    @Transactional
    public UserVoteDetailsResponse voteUserVote(Long userVoteId, SelectUserVoteOptionRequest request) {
        validateUserVoteRequest(request);
        UserVote userVote = findUserVote(userVoteId);
        assertVisible(userVote);
        findProfile(request.getProfileId());
        UserVoteOption option = userVoteOptionRepository.findById(request.getUserVoteOptionId())
                .orElseThrow(() -> new IllegalArgumentException("User vote option not found. userVoteOptionId=" + request.getUserVoteOptionId()));
        if (!option.getUserVote().getId().equals(userVoteId)) {
            throw new IllegalArgumentException("User vote option does not belong to this user vote.");
        }
        if (userVoteSelectionRepository.existsByUserVoteIdAndProfileId(userVoteId, request.getProfileId())) {
            throw new IllegalArgumentException("Already voted user vote.");
        }

        userVoteSelectionRepository.save(UserVoteSelection.builder()
                .userVote(userVote)
                .userVoteOption(option)
                .profileId(request.getProfileId())
                .build());
        userVote.increaseVoteCount();
        option.increaseVoteCount();

        return toUserVoteDetails(userVoteId, userVote, request.getProfileId());
    }

    @Transactional
    public CommunityLikeToggleResponse togglePostLike(CommunityPostType postType, Long postId, ToggleCommunityLikeRequest request) {
        validateProfileOnlyRequest(request);
        CommunityPost post = findCommunityPost(postType, postId);
        findProfile(request.getProfileId());

        return communityPostLikeRepository.findByPostTypeAndPostIdAndProfileId(postType, postId, request.getProfileId())
                .map(like -> {
                    communityPostLikeRepository.delete(like);
                    post.decreaseLikeCount();
                    return new CommunityLikeToggleResponse(false, post.getLikeCount());
                })
                .orElseGet(() -> {
                    communityPostLikeRepository.save(CommunityPostLike.builder()
                            .postType(postType)
                            .postId(postId)
                            .profileId(request.getProfileId())
                            .build());
                    post.increaseLikeCount();
                    notifyPostLike(post, request.getProfileId());
                    return new CommunityLikeToggleResponse(true, post.getLikeCount());
                });
    }

    @Transactional
    public CommunityCommentResponse createComment(CommunityPostType postType, Long postId, CreateCommunityCommentRequest request) {
        validateCommentRequest(request);
        CommunityPost post = findCommunityPost(postType, postId);
        Profile profile = findProfile(request.getProfileId());

        CommunityComment comment = communityCommentRepository.save(CommunityComment.toEntity(postType, postId, request));
        post.increaseCommentCount();
        profile.addActivityPoint(COMMENT_CREATE_ACTIVITY_POINT);
        notifyPostComment(post, comment);

        return CommunityCommentResponse.from(comment, profile);
    }

    @Transactional
    public CommunityLikeToggleResponse toggleCommentLike(CommunityPostType postType, Long communityCommentId, ToggleCommunityLikeRequest request) {
        validateProfileOnlyRequest(request);
        CommunityComment comment = findComment(communityCommentId, postType);
        findProfile(request.getProfileId());

        return communityCommentLikeRepository.findByCommunityCommentIdAndProfileId(communityCommentId, request.getProfileId())
                .map(like -> {
                    communityCommentLikeRepository.delete(like);
                    comment.decreaseLikeCount();
                    return new CommunityLikeToggleResponse(false, comment.getLikeCount());
                })
                .orElseGet(() -> {
                    communityCommentLikeRepository.save(CommunityCommentLike.builder()
                            .communityCommentId(communityCommentId)
                            .profileId(request.getProfileId())
                            .build());
                    comment.increaseLikeCount();
                    notifyCommentLike(comment, request.getProfileId());
                    return new CommunityLikeToggleResponse(true, comment.getLikeCount());
                });
    }

    private void notifyPostLike(CommunityPost post, Long actorProfileId) {
        if (!post.getProfileId().equals(actorProfileId)) {
            pushNotificationService.notify(
                    post.getProfileId(),
                    PushNotificationType.LIKE,
                    "좋아요",
                    post.getTitle() + " 글을 좋아해요.",
                    post.getClass().getSimpleName().toUpperCase(),
                    post.getId()
            );
        }
    }

    private void notifyPostComment(CommunityPost post, CommunityComment comment) {
        if (!post.getProfileId().equals(comment.getProfileId())) {
            pushNotificationService.notify(
                    post.getProfileId(),
                    PushNotificationType.COMMENT,
                    "새 댓글",
                    post.getTitle() + " 글에 새로운 댓글이 달렸어요.",
                    comment.getPostType().name(),
                    comment.getPostId()
            );
        }
    }

    private void notifyCommentLike(CommunityComment comment, Long actorProfileId) {
        if (!comment.getProfileId().equals(actorProfileId)) {
            pushNotificationService.notify(
                    comment.getProfileId(),
                    PushNotificationType.LIKE,
                    "좋아요",
                    comment.getContent(),
                    comment.getPostType().name(),
                    comment.getPostId()
            );
        }
    }

    private UserVoteDetailsResponse toUserVoteDetails(Long userVoteId, UserVote userVote, Long profileId) {
        List<UserVoteOption> options = userVoteOptionRepository.findByUserVoteIdOrderByDisplayOrderAsc(userVoteId);
        UserVoteOption selectedOption = profileId == null ? null : userVoteSelectionRepository.findByUserVoteIdAndProfileId(userVoteId, profileId)
                .map(UserVoteSelection::getUserVoteOption)
                .orElse(null);
        return UserVoteDetailsResponse.from(userVote, findProfile(userVote.getProfileId()), options, selectedOption, getComments(CommunityPostType.USER_VOTE, userVoteId));
    }

    private void increaseViewCountIfFirstView(CommunityPostType postType, Long postId, Long profileId, CommunityPost post) {
        if (profileId == null || communityPostViewRepository.existsByPostTypeAndPostIdAndProfileId(postType, postId, profileId)) {
            return;
        }
        communityPostViewRepository.save(CommunityPostView.create(postType, postId, profileId));
        post.increaseViewCount();
    }

    private List<CommunityCommentResponse> getComments(CommunityPostType postType, Long postId) {
        return communityCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(postType, postId).stream()
                .filter(CommunityComment::isVisible)
                .map(comment -> CommunityCommentResponse.from(comment, findProfile(comment.getProfileId())))
                .toList();
    }

    private void deleteCommunityRelations(CommunityPostType postType, Long postId) {
        communityCommentRepository.findByPostTypeAndPostId(postType, postId)
                .forEach(comment -> communityCommentLikeRepository.deleteByCommunityCommentId(comment.getId()));
        communityCommentRepository.deleteByPostTypeAndPostId(postType, postId);
        communityPostLikeRepository.deleteByPostTypeAndPostId(postType, postId);
    }

    private void saveUserVoteOptions(UserVote userVote, List<CreateUserVoteRequest.UserVoteOptionRequest> options) {
        saveUserVoteOptionContents(userVote, options.stream()
                .map(CreateUserVoteRequest.UserVoteOptionRequest::getContent)
                .toList());
    }

    private void saveUpdatedUserVoteOptions(UserVote userVote, List<UpdateUserVoteRequest.UserVoteOptionRequest> options) {
        saveUserVoteOptionContents(userVote, options.stream()
                .map(UpdateUserVoteRequest.UserVoteOptionRequest::getContent)
                .toList());
    }

    private void saveUserVoteOptionContents(UserVote userVote, List<String> optionContents) {
        userVoteOptionRepository.saveAll(IntStream.range(0, optionContents.size())
                .mapToObj(index -> UserVoteOption.builder()
                        .userVote(userVote)
                        .content(optionContents.get(index).trim())
                        .displayOrder(index + 1)
                        .voteCount(0)
                        .build())
                .toList());
    }

    private CommunityPost findCommunityPost(CommunityPostType postType, Long postId) {
        CommunityPost post = switch (postType) {
            case THREAD -> findThread(postId);
            case USER_VOTE -> findUserVote(postId);
        };
        assertVisible(post);
        return post;
    }

    private void assertPostOwner(CommunityPost post, Long profileId) {
        if (!post.getProfileId().equals(profileId)) {
            throw new IllegalArgumentException("Only the author can modify or delete this community post.");
        }
    }

    private void assertVisible(CommunityPost post) {
        if (!post.isVisible()) {
            throw new IllegalArgumentException("Hidden community post cannot be accessed.");
        }
    }

    private Thread findThread(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found. threadId=" + threadId));
    }

    private UserVote findUserVote(Long userVoteId) {
        return userVoteRepository.findById(userVoteId)
                .orElseThrow(() -> new IllegalArgumentException("User vote not found. userVoteId=" + userVoteId));
    }

    private CommunityComment findComment(Long communityCommentId, CommunityPostType postType) {
        CommunityComment comment = communityCommentRepository.findById(communityCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Community comment not found. communityCommentId=" + communityCommentId));
        if (comment.getPostType() != postType) {
            throw new IllegalArgumentException("Community comment does not belong to " + postType + ".");
        }
        if (!comment.isVisible()) {
            throw new IllegalArgumentException("Hidden community comment cannot be accessed.");
        }
        return comment;
    }

    private Program findProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found. programId=" + programId));
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private Mission findMission(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found. missionId=" + missionId));
    }

    private void validateCreateThreadRequest(CreateThreadRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
        if (request.getProgramId() == null && request.getMissionId() == null) throw new IllegalArgumentException("programId or missionId is required.");
        if (request.getTitle() == null || request.getTitle().isBlank()) throw new IllegalArgumentException("title is required.");
        if (request.getDescription() == null || request.getDescription().isBlank()) throw new IllegalArgumentException("description is required.");
        validateThreadImageUrls(request.getImageUrls());
    }

    private void validateUpdateThreadRequest(UpdateThreadRequest request) {
        if (request.getTitle() != null && request.getTitle().isBlank()) throw new IllegalArgumentException("title cannot be blank.");
        if (request.getDescription() != null && request.getDescription().isBlank()) throw new IllegalArgumentException("description cannot be blank.");
        validateThreadImageUrls(request.getImageUrls());
    }

    private void validateUpdateUserVoteRequest(UpdateUserVoteRequest request) {
        if (request.getTitle() != null && request.getTitle().isBlank()) throw new IllegalArgumentException("title cannot be blank.");
        if (request.getDescription() != null && request.getDescription().isBlank()) throw new IllegalArgumentException("description cannot be blank.");
        if (request.getOptions() != null && request.getOptions().size() < 2) throw new IllegalArgumentException("At least two options are required.");
        if (request.getOptions() != null && request.getOptions().stream().anyMatch(option -> option == null || option.getContent() == null || option.getContent().isBlank())) {
            throw new IllegalArgumentException("Option content is required.");
        }
    }

    private void validateCreateUserVoteRequest(CreateUserVoteRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
        if (request.getProgramId() == null) throw new IllegalArgumentException("programId is required.");
        if (request.getTitle() == null || request.getTitle().isBlank()) throw new IllegalArgumentException("title is required.");
        if (request.getOptions() == null || request.getOptions().size() < 2) throw new IllegalArgumentException("At least two options are required.");
        if (request.getOptions().stream().anyMatch(option -> option == null || option.getContent() == null || option.getContent().isBlank())) {
            throw new IllegalArgumentException("Option content is required.");
        }
    }

    private void validateUserVoteRequest(SelectUserVoteOptionRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
        if (request.getUserVoteOptionId() == null) throw new IllegalArgumentException("userVoteOptionId is required.");
    }

    private void validateCommentRequest(CreateCommunityCommentRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
        if (request.getContent() == null || request.getContent().isBlank()) throw new IllegalArgumentException("content is required.");
    }

    private void validateProfileOnlyRequest(ToggleCommunityLikeRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
    }

    private void validateThreadImageUrls(List<String> imageUrls) {
        if (imageUrls != null && imageUrls.size() > 5) {
            throw new IllegalArgumentException("imageUrls can contain up to 5 images.");
        }
        if (imageUrls != null && imageUrls.stream().anyMatch(imageUrl -> imageUrl == null || imageUrl.isBlank())) {
            throw new IllegalArgumentException("imageUrl cannot be blank.");
        }
    }
}
