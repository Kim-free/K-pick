package com.example.kpick.ranking.service;

import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.ranking.domain.RankingSeason;
import com.example.kpick.ranking.domain.RankingSeasonStatus;
import com.example.kpick.ranking.domain.SeasonReward;
import com.example.kpick.ranking.domain.SeasonRewardStatus;
import com.example.kpick.ranking.dto.req.CreateRankingSeasonRequest;
import com.example.kpick.ranking.dto.res.AdminRankingSeasonResponse;
import com.example.kpick.ranking.dto.res.AdminSeasonRewardResponse;
import com.example.kpick.ranking.dto.res.SeasonCloseResponse;
import com.example.kpick.ranking.dto.res.SeasonRewardSendResponse;
import com.example.kpick.ranking.repository.RankingSeasonRepository;
import com.example.kpick.ranking.repository.SeasonRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RankingAdminService {
    private static final int SEASON_DECAY_RATE = 30;

    private final RankingSeasonRepository rankingSeasonRepository;
    private final SeasonRewardRepository seasonRewardRepository;
    private final ProfileRepository profileRepository;
    private final MissionAttenderRepository missionAttenderRepository;

    @Transactional
    public AdminRankingSeasonResponse createSeason(CreateRankingSeasonRequest request) {
        validateCreateSeasonRequest(request);
        if (rankingSeasonRepository.existsBySeasonStatus(RankingSeasonStatus.ACTIVE)) {
            throw new IllegalArgumentException("Active ranking season already exists.");
        }
        return toSeasonResponse(rankingSeasonRepository.save(RankingSeason.create(request)));
    }

    @Transactional(readOnly = true)
    public List<AdminRankingSeasonResponse> getSeasons() {
        return rankingSeasonRepository.findAllByOrderByStartDateDesc().stream()
                .map(this::toSeasonResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminSeasonRewardResponse> getSeasonRewards(Long rankingSeasonId) {
        RankingSeason season = findSeason(rankingSeasonId);
        List<SeasonReward> rewards = seasonRewardRepository.findByRankingSeasonIdOrderByRankAsc(rankingSeasonId);
        return rewards.isEmpty()
                ? createCurrentRewardPreview(season)
                : toRewardResponses(rewards);
    }

    @Transactional
    public SeasonCloseResponse closeCurrentSeason() {
        RankingSeason season = rankingSeasonRepository.findFirstBySeasonStatus(RankingSeasonStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Active ranking season not found."));
        snapshotRewards(season);
        season.close();
        List<Profile> profiles = profileRepository.findAll();
        profiles.forEach(Profile::applySeasonPointDecay);
        return new SeasonCloseResponse("Current season closed. Points decreased by 30%.", profiles.size(), SEASON_DECAY_RATE);
    }

    @Transactional
    public SeasonRewardSendResponse sendAllRewards(Long rankingSeasonId) {
        RankingSeason season = findEndedSeason(rankingSeasonId);
        snapshotRewards(season);
        List<SeasonReward> rewards = seasonRewardRepository.findByRankingSeasonIdOrderByRankAsc(rankingSeasonId);
        int sentCount = 0;
        for (SeasonReward reward : rewards) {
            if (reward.getRewardStatus() != SeasonRewardStatus.SENT) {
                reward.markSent();
                sentCount++;
            }
        }
        return new SeasonRewardSendResponse(rankingSeasonId, sentCount);
    }

    @Transactional
    public AdminSeasonRewardResponse sendReward(Long rankingSeasonId, Long seasonRewardId) {
        findEndedSeason(rankingSeasonId);
        SeasonReward reward = seasonRewardRepository.findById(seasonRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Season reward not found. seasonRewardId=" + seasonRewardId));
        if (!reward.getRankingSeasonId().equals(rankingSeasonId)) {
            throw new IllegalArgumentException("Season reward does not belong to ranking season.");
        }
        if (reward.getRewardStatus() != SeasonRewardStatus.SENT) {
            reward.markSent();
        }
        return toRewardResponse(reward, getNickname(reward.getProfileId()));
    }

    private AdminRankingSeasonResponse toSeasonResponse(RankingSeason season) {
        LocalDateTime startDateTime = season.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = season.getEndDate().plusDays(1).atStartOfDay().minusNanos(1);
        return AdminRankingSeasonResponse.from(
                season,
                missionAttenderRepository.countDistinctProfilesByCreatedAtBetween(startDateTime, endDateTime),
                missionAttenderRepository.countByCreatedAtBetween(startDateTime, endDateTime)
        );
    }

    private void snapshotRewards(RankingSeason season) {
        if (seasonRewardRepository.existsByRankingSeasonId(season.getId())) {
            return;
        }
        List<Profile> rankedProfiles = getTopProfiles(season.getRewardTopN());
        seasonRewardRepository.saveAll(IntStream.range(0, rankedProfiles.size())
                .mapToObj(index -> {
                    Profile profile = rankedProfiles.get(index);
                    return SeasonReward.builder()
                            .rankingSeasonId(season.getId())
                            .profileId(profile.getId())
                            .rank(index + 1)
                            .seasonPoint(profile.getMissionPointValue())
                            .rewardDescription(season.getRewardDescription())
                            .recipientInfoSubmitted(false)
                            .rewardStatus(SeasonRewardStatus.NOT_SENT)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .toList());
    }

    private List<AdminSeasonRewardResponse> createCurrentRewardPreview(RankingSeason season) {
        List<Profile> profiles = getTopProfiles(season.getRewardTopN());
        return IntStream.range(0, profiles.size())
                .mapToObj(index -> {
                    Profile profile = profiles.get(index);
                    return new AdminSeasonRewardResponse(
                            null,
                            profile.getId(),
                            index + 1,
                            profile.getNickname(),
                            profile.getMissionPointValue(),
                            season.getRewardDescription(),
                            false,
                            SeasonRewardStatus.NOT_SENT
                    );
                })
                .toList();
    }

    private List<Profile> getTopProfiles(int rewardTopN) {
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparingLong(Profile::getMissionPointValue).reversed())
                .limit(rewardTopN)
                .toList();
    }

    private List<AdminSeasonRewardResponse> toRewardResponses(List<SeasonReward> rewards) {
        Map<Long, Profile> profileMap = profileRepository.findAllById(
                        rewards.stream().map(SeasonReward::getProfileId).toList()
                ).stream()
                .collect(java.util.stream.Collectors.toMap(Profile::getId, Function.identity()));
        return rewards.stream()
                .map(reward -> toRewardResponse(reward, profileMap.containsKey(reward.getProfileId())
                        ? profileMap.get(reward.getProfileId()).getNickname()
                        : null))
                .toList();
    }

    private AdminSeasonRewardResponse toRewardResponse(SeasonReward reward, String nickname) {
        return new AdminSeasonRewardResponse(
                reward.getId(),
                reward.getProfileId(),
                reward.getRank(),
                nickname,
                reward.getSeasonPoint(),
                reward.getRewardDescription(),
                Boolean.TRUE.equals(reward.getRecipientInfoSubmitted()),
                reward.getRewardStatus()
        );
    }

    private String getNickname(Long profileId) {
        return profileRepository.findById(profileId)
                .map(Profile::getNickname)
                .orElse(null);
    }

    private RankingSeason findEndedSeason(Long rankingSeasonId) {
        RankingSeason season = findSeason(rankingSeasonId);
        if (season.getSeasonStatus() != RankingSeasonStatus.ENDED) {
            throw new IllegalArgumentException("Rewards can be sent after ranking season ends.");
        }
        return season;
    }

    private RankingSeason findSeason(Long rankingSeasonId) {
        return rankingSeasonRepository.findById(rankingSeasonId)
                .orElseThrow(() -> new IllegalArgumentException("Ranking season not found. rankingSeasonId=" + rankingSeasonId));
    }

    private void validateCreateSeasonRequest(CreateRankingSeasonRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getSeasonName() == null || request.getSeasonName().isBlank()) throw new IllegalArgumentException("seasonName is required.");
        if (request.getStartDate() == null) throw new IllegalArgumentException("startDate is required.");
        if (request.getEndDate() == null) throw new IllegalArgumentException("endDate is required.");
        if (request.getEndDate().isBefore(request.getStartDate())) throw new IllegalArgumentException("endDate cannot be before startDate.");
        if (request.getRewardTopN() == null || request.getRewardTopN() < 1) throw new IllegalArgumentException("rewardTopN must be positive.");
        if (request.getRewardDescription() == null || request.getRewardDescription().isBlank()) {
            throw new IllegalArgumentException("rewardDescription is required.");
        }
    }
}
