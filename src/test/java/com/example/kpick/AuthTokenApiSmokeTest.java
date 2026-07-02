package com.example.kpick;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.domain.LoginType;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.domain.ResultPublishTiming;
import com.example.kpick.mission.repository.MissionOptionRepository;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.domain.SignUpStatus;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.repository.ProgramRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "auth.dev-bypass.enabled=false")
@AutoConfigureMockMvc
@Transactional
class AuthTokenApiSmokeTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private com.example.kpick.appUser.repository.AppUserRepository appUserRepository;

    @Autowired
    private com.example.kpick.profile.repository.ProfileRepository profileRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionOptionRepository missionOptionRepository;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private UserVoteRepository userVoteRepository;

    @Test
    void appUserId로_발급한_토큰으로_일반_api들을_호출할_수_있다() throws Exception {
        TestFixture fixture = createFixture();
        String accessToken = issueTestToken(fixture.appUser.getId());

        callReadApis(accessToken, fixture);
        callProfileApis(accessToken, fixture);
        callBenefitApis(accessToken, fixture);
        callNotificationApis(accessToken);
        callMissionApis(accessToken, fixture);
        callCommunityApis(accessToken, fixture);
        callSupportApis(accessToken, fixture);
        callAdminApis(issueTestToken(fixture.adminAppUser.getId()), fixture);
        callWithdrawApi(accessToken);
    }

    private void callReadApis(String accessToken, TestFixture fixture) throws Exception {
        authorizedGet(accessToken, "/api/programs");
        authorizedGet(accessToken, "/api/programs/selections");
        authorizedGet(accessToken, "/api/programs/selections?keyword=Smoke");
        authorizedGet(accessToken, "/api/programs/" + fixture.program.getId() + "/episodes");
        authorizedGet(accessToken, "/api/notices");
        authorizedGet(accessToken, "/api/rankings");
        authorizedGet(accessToken, "/api/rankings?rankingType=COMMUNITY");
        authorizedGet(accessToken, "/api/rankings?rankingType=TOTAL");
        authorizedGet(accessToken, "/api/rankings/profiles/" + fixture.profile.getId());
        authorizedGet(accessToken, "/api/rankings/profiles/me/growth");
        authorizedGet(accessToken, "/api/community");
    }

    private void callProfileApis(String accessToken, TestFixture fixture) throws Exception {
        authorizedPatch(accessToken, "/api/profiles/me/onboarding",
                "{\"nickname\":\"SmokeOnboard\",\"profileImageUrl\":\"https://example.com/onboarding.png\","
                        + "\"gender\":\"OTHER\",\"birthDate\":\"2000-01-01\",\"joinPath\":\"INSTAGRAM\","
                        + "\"friendInviteCode\":\"" + fixture.targetProfile.getInviteCode() + "\"}");
        assertEquals(600L, getProfileCoin(fixture.profile.getId()));
        assertEquals(200L, getProfileCoin(fixture.targetProfile.getId()));
        authorizedGet(accessToken, "/api/profiles/me/my-page");
        authorizedGet(accessToken, "/api/profiles/nickname/check?nickname=SmokeNickname");
        authorizedPatch(accessToken, "/api/profiles/me/nickname", "{\"nickname\":\"SmokeNick\"}");
        authorizedPatch(accessToken, "/api/profiles/me/image", "{\"profileImageUrl\":\"https://example.com/profile.png\"}");
        authorizedGet(accessToken, "/api/profiles/me/mission-history");
        authorizedGet(accessToken, "/api/profiles/me/community-activities");
        authorizedGet(accessToken, "/api/profiles/me/badges");
        authorizedPost(accessToken, "/api/profiles/me/badges", "{\"badgeCode\":\"TEST_BADGE\",\"badgeName\":\"테스트뱃지\",\"description\":\"테스트\",\"emoji\":\"T\"}");
        authorizedGet(accessToken, "/api/profiles/me/program-interests");
        authorizedGet(accessToken, "/api/profiles/me/program-interests/search?keyword=Smoke");
        authorizedPut(accessToken, "/api/profiles/me/program-interests", "{\"programIds\":[" + fixture.program.getId() + "]}");
    }

    private void callBenefitApis(String accessToken, TestFixture fixture) throws Exception {
        authorizedGet(accessToken, "/api/benefits/me");
        authorizedPost(accessToken, "/api/benefits/me/attendance", "{}");
        authorizedPost(accessToken, "/api/benefits/me/ad-reward", "{}");
        long coinBeforeAdMobCallback = getProfileCoin(fixture.profile.getId());
        String adMobCallbackUrl = "/v1/ads/admob/callback"
                + "?user_id=" + fixture.profile.getId()
                + "&reward_amount=7"
                + "&reward_item=Pick"
                + "&ad_network=admob"
                + "&ad_unit=test-ad-unit"
                + "&transaction_id=smoke-admob-transaction"
                + "&signature=abcdefghijklmnopqrstuvwxyz"
                + "&key_id=test-key"
                + "&timestamp=1760000000000";
        mockMvc.perform(get(adMobCallbackUrl))
                .andExpect(status().isOk());
        mockMvc.perform(get(adMobCallbackUrl))
                .andExpect(status().isOk());
        assertEquals(coinBeforeAdMobCallback + 7L, getProfileCoin(fixture.profile.getId()));
    }

    private void callNotificationApis(String accessToken) throws Exception {
        authorizedGet(accessToken, "/api/notifications/me/settings");
        authorizedPatch(accessToken, "/api/notifications/me/settings", "{\"commentEnabled\":true,\"likeEnabled\":true}");
        authorizedPost(accessToken, "/api/notifications/me/device-tokens", "{\"deviceToken\":\"smoke-fcm-token\",\"platform\":\"IOS\"}");
        authorizedGet(accessToken, "/api/notifications/me");
    }

    private void callMissionApis(String accessToken, TestFixture fixture) throws Exception {
        authorizedGet(accessToken, "/api/missions/recommendations");
        authorizedGet(accessToken, "/api/missions/recommendations?programId=" + fixture.program.getId());
        authorizedGet(accessToken, "/api/missions/" + fixture.mission.getId() + "/related");
        authorizedGet(accessToken, "/api/missions/" + fixture.mission.getId());
        authorizedPost(accessToken, "/api/missions/" + fixture.mission.getId() + "/options/select",
                "{\"missionOptionId\":" + fixture.missionOption.getId() + "}");
        authorizedPost(accessToken, "/api/mission-suggestions",
                "{\"programId\":" + fixture.program.getId() + ",\"episode\":\"1화\",\"genre\":\"DATINGSHOW\",\"missionTitle\":\"테스트 미션 건의\",\"options\":[{\"content\":\"A\"},{\"content\":\"B\"}]}");
    }

    private void callCommunityApis(String accessToken, TestFixture fixture) throws Exception {
        Long threadPostId = extractLong(authorizedPost(accessToken, "/api/community",
                "{\"postType\":\"THREAD\",\"programId\":" + fixture.program.getId()
                        + ",\"title\":\"테스트 스레드\",\"description\":\"테스트 스레드 내용\",\"isNicknamePublic\":true,\"imageUrls\":[]}"), "postId");

        authorizedGet(accessToken, "/api/community/posts/" + threadPostId + "?postType=THREAD");
        assertEquals(1, getThreadViewCount(threadPostId));
        authorizedGet(accessToken, "/api/community/posts/" + threadPostId + "?postType=THREAD");
        assertEquals(1, getThreadViewCount(threadPostId));
        authorizedPatch(accessToken, "/api/community/THREAD/" + threadPostId,
                "{\"title\":\"수정된 테스트 스레드\",\"description\":\"수정된 내용\"}");
        authorizedPost(accessToken, "/api/community/THREAD/" + threadPostId + "/likes", "{}");
        assertEquals(1, getThreadViewCount(threadPostId));
        authorizedPost(accessToken, "/api/community/THREAD/" + threadPostId + "/likes", "{}");
        assertEquals(1, getThreadViewCount(threadPostId));
        Long commentId = extractLong(authorizedPost(accessToken, "/api/community/THREAD/" + threadPostId + "/comments",
                "{\"content\":\"테스트 댓글\"}"), "communityCommentId");
        authorizedPost(accessToken, "/api/community/THREAD/comments/" + commentId + "/likes", "{}");
        assertEquals(1, getThreadViewCount(threadPostId));

        Long userVotePostId = extractLong(authorizedPost(accessToken, "/api/community",
                "{\"postType\":\"USER_VOTE\",\"programId\":" + fixture.program.getId()
                        + ",\"title\":\"테스트 유저투표\",\"description\":\"투표 내용\",\"options\":[{\"content\":\"A\"},{\"content\":\"B\"}]}"), "postId");
        fixture.reportTargetPostId = userVotePostId;
        JsonNode userVoteDetails = objectMapper.readTree(authorizedGet(accessToken, "/api/community/posts/" + userVotePostId + "?postType=USER_VOTE"));
        assertEquals(1, getUserVoteViewCount(userVotePostId));
        authorizedGet(accessToken, "/api/community/posts/" + userVotePostId + "?postType=USER_VOTE");
        assertEquals(1, getUserVoteViewCount(userVotePostId));
        authorizedPost(accessToken, "/api/community/USER_VOTE/" + userVotePostId + "/likes", "{}");
        assertEquals(1, getUserVoteViewCount(userVotePostId));
        Long userVoteOptionId = userVoteDetails.get("options").get(0).get("userVoteOptionId").asLong();
        authorizedPost(accessToken, "/api/user-votes/" + userVotePostId + "/vote", "{\"userVoteOptionId\":" + userVoteOptionId + "}");
        authorizedGet(accessToken, "/api/user-votes/" + userVotePostId + "/result");

        authorizedDelete(accessToken, "/api/community/THREAD/" + threadPostId);
    }

    private void callSupportApis(String accessToken, TestFixture fixture) throws Exception {
        authorizedPost(accessToken, "/api/inquiries",
                "{\"inquiryType\":\"SERVICE_USAGE\",\"title\":\"테스트 문의\",\"content\":\"문의 내용입니다.\",\"imageUrls\":[],\"replyEmail\":\"test@example.com\",\"privacyAgreed\":true}");
        authorizedPost(accessToken, "/api/reports",
                "{\"targetType\":\"USER\",\"targetId\":" + fixture.targetProfile.getId()
                        + ",\"reason\":\"SPAM\",\"reasonDetail\":\"테스트 신고\"}");
        authorizedPost(accessToken, "/api/images/presigned-url",
                "{\"uploadPurpose\":\"PROGRAM\",\"fileName\":\"thumbnail.png\",\"contentType\":\"image/png\",\"fileSize\":1024}");
    }

    private void callAdminApis(String adminAccessToken, TestFixture fixture) throws Exception {
        Long adminProgramId = extractLong(authorizedPost(adminAccessToken, "/api/admin/programs",
                "{\"programName\":\"Admin Smoke Program\",\"broadcaster\":\"Kpick\",\"genre\":\"MUSIC\",\"season\":\"시즌1\","
                        + "\"episodeCount\":3,\"broadcastStartDate\":\"2026-06-01\",\"broadcastEndDate\":\"2026-06-30\","
                        + "\"thumbnailImageUrl\":\"https://example.com/admin-program.png\",\"isOnAir\":true,\"isExposed\":true,\"description\":\"어드민 테스트\"}"), "programId");
        authorizedGet(adminAccessToken, "/api/admin/programs");
        authorizedGet(adminAccessToken, "/api/admin/programs/" + adminProgramId + "/details");
        authorizedPatch(adminAccessToken, "/api/admin/programs/" + adminProgramId,
                "{\"description\":\"수정된 어드민 테스트\",\"isExposed\":true}");

        JsonNode adminMission = objectMapper.readTree(authorizedPost(adminAccessToken, "/api/admin/missions",
                "{\"programId\":" + adminProgramId + ",\"missionName\":\"어드민 테스트 미션\",\"episode\":\"1화\","
                        + "\"dueDateTime\":\"2026-06-10\",\"coinFee\":30,\"isActive\":true,"
                        + "\"options\":[{\"content\":\"A\"},{\"content\":\"B\"}]}"));
        Long adminMissionId = adminMission.get("missionId").asLong();
        Long adminMissionOptionId = adminMission.get("options").get(0).get("missionOptionId").asLong();
        authorizedGet(adminAccessToken, "/api/admin/missions");
        authorizedPatch(adminAccessToken, "/api/admin/missions/" + adminMissionId + "/confirm-result",
                "{\"correctMissionOptionId\":" + adminMissionOptionId + ",\"resultPublishTiming\":\"IMMEDIATE\"}");

        Long noticeId = extractLong(authorizedPost(adminAccessToken, "/api/admin/notices",
                "{\"title\":\"테스트 공지\",\"content\":\"공지 내용\",\"noticeStatus\":\"PUBLISHED\"}"), "noticeId");
        authorizedGet(adminAccessToken, "/api/admin/notices");
        authorizedPatch(adminAccessToken, "/api/admin/notices/" + noticeId + "/status", "{\"noticeStatus\":\"HIDDEN\"}");

        authorizedGet(adminAccessToken, "/api/admin/benefits/pick-histories");
        authorizedGet(adminAccessToken, "/api/admin/benefits/setting");
        authorizedPatch(adminAccessToken, "/api/admin/benefits/setting",
                "{\"dailyAttendancePick\":3,\"weeklyAttendanceBonusPick\":3,\"dailyAdLimit\":10,\"adRewardPick\":1}");

        authorizedGet(adminAccessToken, "/api/admin/users");
        authorizedGet(adminAccessToken, "/api/admin/users/" + fixture.profile.getId());
        authorizedGet(adminAccessToken, "/api/admin/reports");
        authorizedPost(adminAccessToken, "/api/admin/users/" + fixture.targetProfile.getId() + "/sanctions",
                "{\"sanctionType\":\"WARNING\",\"reason\":\"스모크 테스트 경고\",\"notifyUser\":false}");

        authorizedGet(adminAccessToken, "/api/admin/community/posts");
        authorizedGet(adminAccessToken, "/api/admin/community/posts/USER_VOTE/" + fixture.reportTargetPostId);
        authorizedPatch(adminAccessToken, "/api/admin/community/posts/USER_VOTE/" + fixture.reportTargetPostId + "/status",
                "{\"contentStatus\":\"NORMAL\"}");
        authorizedGet(adminAccessToken, "/api/admin/community/comments");

        Long inquiryId = extractLong(authorizedPost(adminAccessToken, "/api/inquiries",
                "{\"inquiryType\":\"SERVICE_USAGE\",\"title\":\"어드민 테스트 문의\",\"content\":\"문의 내용입니다.\",\"imageUrls\":[],\"replyEmail\":\"admin-test@example.com\",\"privacyAgreed\":true}"), "inquiryId");
        authorizedGet(adminAccessToken, "/api/admin/inquiries");
        authorizedGet(adminAccessToken, "/api/admin/inquiries/" + inquiryId);
        authorizedPatch(adminAccessToken, "/api/admin/inquiries/" + inquiryId + "/draft", "{\"answerDraft\":\"임시 답변\"}");

        Long seasonId = extractLong(authorizedPost(adminAccessToken, "/api/admin/rankings/seasons",
                "{\"seasonName\":\"스모크 시즌\",\"startDate\":\"2026-07-01\",\"endDate\":\"2026-07-31\","
                        + "\"rewardTopN\":3,\"rewardDescription\":\"스모크 보상\"}"), "rankingSeasonId");
        authorizedGet(adminAccessToken, "/api/admin/rankings/seasons");
        authorizedGet(adminAccessToken, "/api/admin/rankings/seasons/" + seasonId + "/rewards");
        authorizedPost(adminAccessToken, "/api/admin/notifications/broadcast",
                "{\"notificationType\":\"EVENT_NOTICE\",\"title\":\"테스트 알림\",\"body\":\"테스트 본문\",\"targetType\":\"NOTICE\",\"targetId\":" + noticeId + "}");
    }

    private void callWithdrawApi(String accessToken) throws Exception {
        authorizedDelete(accessToken, "/api/app-users/me");
        mockMvc.perform(get("/api/profiles/me/my-page").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnauthorized());
    }

    private String issueTestToken(Long appUserId) throws Exception {
        String response = mockMvc.perform(post("/api/auth/test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appUserId\":" + appUserId + "}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String authorizedGet(String accessToken, String url) throws Exception {
        return mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String authorizedPost(String accessToken, String url, String body) throws Exception {
        return mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void authorizedPatch(String accessToken, String url, String body) throws Exception {
        mockMvc.perform(patch(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful());
    }

    private void authorizedPut(String accessToken, String url, String body) throws Exception {
        mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful());
    }

    private void authorizedDelete(String accessToken, String url) throws Exception {
        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().is2xxSuccessful());
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private Long extractLong(String json, String fieldName) throws Exception {
        JsonNode value = objectMapper.readTree(json).get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalStateException("Missing field: " + fieldName + ", json=" + json);
        }
        return value.asLong();
    }

    private int getThreadViewCount(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalStateException("Missing thread: " + threadId))
                .getViewCount();
    }

    private int getUserVoteViewCount(Long userVoteId) {
        return userVoteRepository.findById(userVoteId)
                .orElseThrow(() -> new IllegalStateException("Missing user vote: " + userVoteId))
                .getViewCount();
    }

    private long getProfileCoin(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("Missing profile: " + profileId))
                .getCoin();
    }

    private TestFixture createFixture() {
        AppUser appUser = appUserRepository.save(AppUser.createOAuthUser("smoke@example.com", LoginType.KAKAO, "smoke-provider", true));
        AppUser adminAppUser = AppUser.createOAuthUser("smoke-admin@example.com", LoginType.KAKAO, "smoke-admin-provider", true);
        adminAppUser.grantAdminRole();
        adminAppUser = appUserRepository.save(adminAppUser);
        Profile profile = profileRepository.save(Profile.builder()
                .appUserId(appUser.getId())
                .nickname("스모크유저")
                .coin(500L)
                .missionPoint(120L)
                .totalMissionPoint(300L)
                .activityPoint(50L)
                .signUpStatus(SignUpStatus.COMPLETE)
                .build());
        profileRepository.save(Profile.builder()
                .appUserId(adminAppUser.getId())
                .nickname("스모크관리자")
                .coin(100L)
                .missionPoint(0L)
                .totalMissionPoint(0L)
                .activityPoint(0L)
                .signUpStatus(SignUpStatus.COMPLETE)
                .build());
        AppUser targetAppUser = appUserRepository.save(AppUser.createOAuthUser("smoke-target@example.com", LoginType.KAKAO, "smoke-target-provider", true));
        Profile targetProfile = profileRepository.save(Profile.builder()
                .appUserId(targetAppUser.getId())
                .nickname("스모크신고대상")
                .coin(100L)
                .missionPoint(10L)
                .totalMissionPoint(10L)
                .activityPoint(0L)
                .signUpStatus(SignUpStatus.COMPLETE)
                .build());
        Program program = programRepository.save(Program.builder()
                .programName("Smoke Program")
                .broadcaster("Kpick")
                .genre(Genre.DATINGSHOW)
                .season("시즌1")
                .episodeCount(5)
                .broadcastStartDate(LocalDate.now())
                .registeredDate(LocalDate.now())
                .thumbnailImageUrl("https://example.com/program.png")
                .isOnAir(true)
                .isExposed(true)
                .description("테스트 프로그램")
                .missionCount(1)
                .build());
        Mission mission = missionRepository.save(Mission.builder()
                .programId(program.getId())
                .profileId(profile.getId())
                .missionName("Smoke Mission")
                .episode("1화")
                .dueDateTime(LocalDateTime.now().plusDays(1))
                .coinFee(50)
                .attenderCount(0)
                .isActive(true)
                .resultPublishTiming(ResultPublishTiming.IMMEDIATE)
                .missionState(MissionState.ONGOING)
                .build());
        MissionOption option = missionOptionRepository.save(MissionOption.builder()
                .mission(mission)
                .content("A")
                .displayOrder(1)
                .isCorrect(false)
                .build());
        missionOptionRepository.save(MissionOption.builder()
                .mission(mission)
                .content("B")
                .displayOrder(2)
                .isCorrect(false)
                .build());
        return new TestFixture(appUser, adminAppUser, profile, targetProfile, program, mission, option, null);
    }

    private static class TestFixture {
        private final AppUser appUser;
        private final AppUser adminAppUser;
        private final Profile profile;
        private final Profile targetProfile;
        private final Program program;
        private final Mission mission;
        private final MissionOption missionOption;
        private Long reportTargetPostId;

        private TestFixture(AppUser appUser, AppUser adminAppUser, Profile profile, Profile targetProfile, Program program, Mission mission, MissionOption missionOption, Long reportTargetPostId) {
            this.appUser = appUser;
            this.adminAppUser = adminAppUser;
            this.profile = profile;
            this.targetProfile = targetProfile;
            this.program = program;
            this.mission = mission;
            this.missionOption = missionOption;
            this.reportTargetPostId = reportTargetPostId;
        }
    }
}
