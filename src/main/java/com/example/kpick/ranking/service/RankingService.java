package com.example.kpick.ranking.service;

import com.example.kpick.community.repository.CommunityCommentRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.ranking.domain.PointScope;
import com.example.kpick.ranking.domain.RankingType;
import com.example.kpick.ranking.dto.res.CommunityRankingResponse;
import com.example.kpick.ranking.dto.res.CommunityRankingResponse.MyCommunityRankingCardResponse;
import com.example.kpick.ranking.dto.res.GrowthRecordResponse;
import com.example.kpick.ranking.dto.res.MyRankingResponse;
import com.example.kpick.ranking.dto.res.PointTierResponse;
import com.example.kpick.ranking.dto.res.RankingEntryResponse;
import com.example.kpick.ranking.dto.res.RankingResponse;
import com.example.kpick.ranking.dto.res.RankingResponse.MyRankingCardResponse;
import com.example.kpick.ranking.dto.res.SeasonCloseResponse;
import com.example.kpick.mission.domain.MissionAttender;
import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RankingService {
    private static final int SEASON_DECAY_RATE = 30;

    private final ProfileRepository profileRepository;
    private final MissionAttenderRepository missionAttenderRepository;
    private final ThreadRepository threadRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserVoteRepository userVoteRepository;

    @Transactional(readOnly = true)
    public Object getRankings(String rankingTypeValue, Long profileId) {
        RankingType rankingType = parseRankingType(rankingTypeValue);
        if (rankingType == RankingType.SEASON) {
            return getSeasonRanking(profileId);
        }
        if (rankingType == RankingType.COMMUNITY) {
            return getCommunityRanking(profileId);
        }
        return getTotalRanking(profileId);
    }

    @Transactional(readOnly = true)
    public RankingResponse getSeasonRanking(Long profileId) {
        return getRanking(RankingType.SEASON, profileId, Profile::getMissionPointValue);
    }

    @Transactional(readOnly = true)
    public RankingResponse getTotalRanking(Long profileId) {
        return getRanking(RankingType.TOTAL, profileId, Profile::getTotalMissionPointValue);
    }

    @Transactional(readOnly = true)
    public CommunityRankingResponse getCommunityRanking(Long profileId) {
        List<Profile> sortedProfiles = getSortedProfiles(Profile::getActivityPointValue);
        List<RankingEntryResponse> rankings = toCommunityRankingEntries(sortedProfiles, Profile::getActivityPointValue);
        MyCommunityRankingCardResponse myRanking = profileId == null ? null : createCommunityMyRanking(profileId, rankings);
        YearMonth currentMonth = YearMonth.now();
        LocalDate periodEndDate = currentMonth.atEndOfMonth();

        return new CommunityRankingResponse(
                RankingType.COMMUNITY,
                "커뮤니티",
                currentMonth.atDay(1),
                periodEndDate,
                daysUntil(periodEndDate),
                sortedProfiles.size(),
                rankings.stream().limit(3).toList(),
                rankings,
                myRanking
        );
    }

    @Transactional(readOnly = true)
    public MyRankingResponse getRankingProfile(Long profileId) {
        Profile profile = findProfile(profileId);
        int seasonRank = findRank(profileId, Profile::getMissionPointValue);
        int totalRank = findRank(profileId, Profile::getTotalMissionPointValue);
        List<MissionAttender> attenders = missionAttenderRepository.findByProfileId(profileId);
        long correctCount = attenders.stream()
                .filter(attender -> Boolean.TRUE.equals(attender.getMissionOption().getIsCorrect()))
                .count();
        int correctRate = attenders.isEmpty() ? 0 : (int) Math.round((correctCount * 100.0) / attenders.size());

        return MyRankingResponse.from(profile, seasonRank, totalRank, correctRate, Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public GrowthRecordResponse getGrowthRecord(Long profileId, String pointScopeValue) {
        Profile profile = findProfile(profileId);
        PointScope pointScope = PointScope.valueOf(pointScopeValue.toUpperCase());
        long point = pointScope == PointScope.SEASON
                ? profile.getMissionPointValue()
                : profile.getTotalMissionPointValue();

        return GrowthRecordResponse.from(profile, pointScope, point);
    }

    @Transactional
    public SeasonCloseResponse closeCurrentSeason() {
        List<Profile> profiles = profileRepository.findAll();
        profiles.forEach(Profile::applySeasonPointDecay);

        return new SeasonCloseResponse("Current season closed. Points decreased by 30%.", profiles.size(), SEASON_DECAY_RATE);
    }

    private RankingResponse getRanking(RankingType rankingType, Long profileId, ToLongFunction<Profile> scoreGetter) {
        List<Profile> sortedProfiles = getSortedProfiles(scoreGetter);
        List<RankingEntryResponse> rankings = toRankingEntries(sortedProfiles, scoreGetter);
        MyRankingCardResponse myRanking = profileId == null ? null : rankings.stream()
                .filter(ranking -> ranking.getProfileId().equals(profileId))
                .findFirst()
                .map(ranking -> new MyRankingCardResponse(
                        ranking.getRank(),
                        ranking.getProfileId(),
                        ranking.getNickname(),
                        ranking.getScore(),
                        ranking.getPointTier(),
                        createNextPointTierMessage(ranking.getScore())
                ))
                .orElse(null);
        YearMonth currentMonth = YearMonth.now();
        String periodName = rankingType == RankingType.TOTAL
                ? "전체"
                : currentMonth.getYear() + " 시즌 " + currentMonth.getMonthValue();
        LocalDate seasonStartDate = rankingType == RankingType.TOTAL ? null : currentMonth.atDay(1);
        LocalDate seasonEndDate = rankingType == RankingType.TOTAL ? null : currentMonth.atEndOfMonth();

        return new RankingResponse(
                rankingType,
                periodName,
                seasonStartDate,
                seasonEndDate,
                seasonEndDate == null ? null : daysUntil(seasonEndDate),
                sortedProfiles.size(),
                rankings.stream().limit(3).toList(),
                rankings,
                myRanking
        );
    }

    private List<Profile> getSortedProfiles(ToLongFunction<Profile> scoreGetter) {
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparingLong(scoreGetter).reversed())
                .toList();
    }

    private List<RankingEntryResponse> toRankingEntries(List<Profile> sortedProfiles, ToLongFunction<Profile> scoreGetter) {
        return IntStream.range(0, sortedProfiles.size())
                .mapToObj(index -> RankingEntryResponse.from(index + 1, sortedProfiles.get(index), scoreGetter.applyAsLong(sortedProfiles.get(index))))
                .toList();
    }

    private List<RankingEntryResponse> toCommunityRankingEntries(List<Profile> sortedProfiles, ToLongFunction<Profile> scoreGetter) {
        return IntStream.range(0, sortedProfiles.size())
                .mapToObj(index -> {
                    Profile profile = sortedProfiles.get(index);
                    long threadCount = threadRepository.countByProfileId(profile.getId())
                            + userVoteRepository.countByProfileId(profile.getId());
                    long commentCount = communityCommentRepository.countByProfileId(profile.getId());
                    return RankingEntryResponse.communityFrom(index + 1, profile, scoreGetter.applyAsLong(profile), threadCount, commentCount);
                })
                .toList();
    }

    private MyCommunityRankingCardResponse createCommunityMyRanking(Long profileId, List<RankingEntryResponse> rankings) {
        RankingEntryResponse ranking = rankings.stream()
                .filter(entry -> entry.getProfileId().equals(profileId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Profile ranking not found. profileId=" + profileId));
        long threadCount = threadRepository.countByProfileId(profileId)
                + userVoteRepository.countByProfileId(profileId);
        long commentCount = communityCommentRepository.countByProfileId(profileId);

        return new MyCommunityRankingCardResponse(
                ranking.getRank(),
                ranking.getProfileId(),
                ranking.getNickname(),
                ranking.getScore(),
                threadCount,
                commentCount,
                "글 " + threadCount + " · 댓글 " + commentCount
        );
    }

    private String createNextPointTierMessage(long score) {
        PointTierResponse pointTier = PointTierResponse.from(score);
        if (pointTier.getNextTierName() == null) {
            return "전설토리 달성";
        }
        return pointTier.getNextTierName() + "까지 " + pointTier.getPointsToNextTier() + "pt 남음";
    }

    private int findRank(Long profileId, ToLongFunction<Profile> scoreGetter) {
        List<Profile> sortedProfiles = getSortedProfiles(scoreGetter);
        return IntStream.range(0, sortedProfiles.size())
                .filter(index -> sortedProfiles.get(index).getId().equals(profileId))
                .map(index -> index + 1)
                .findFirst()
                .orElse(0);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private RankingType parseRankingType(String rankingTypeValue) {
        if (rankingTypeValue == null || rankingTypeValue.isBlank()) {
            return RankingType.SEASON;
        }
        try {
            return RankingType.valueOf(rankingTypeValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("rankingType must be SEASON, COMMUNITY, or TOTAL.");
        }
    }

    private long daysUntil(LocalDate endDate) {
        return Math.max(ChronoUnit.DAYS.between(LocalDate.now(), endDate), 0);
    }
}
