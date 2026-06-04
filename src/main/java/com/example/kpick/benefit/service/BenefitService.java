package com.example.kpick.benefit.service;

import com.example.kpick.benefit.domain.AdRewardHistory;
import com.example.kpick.benefit.domain.AttendanceCheck;
import com.example.kpick.benefit.domain.BenefitSetting;
import com.example.kpick.benefit.domain.PickHistory;
import com.example.kpick.benefit.domain.PickHistoryType;
import com.example.kpick.benefit.dto.req.UpdateBenefitSettingRequest;
import com.example.kpick.benefit.dto.res.AdRewardResponse;
import com.example.kpick.benefit.dto.res.AttendanceCheckResponse;
import com.example.kpick.benefit.dto.res.BenefitHomeResponse;
import com.example.kpick.benefit.dto.res.BenefitSettingResponse;
import com.example.kpick.benefit.dto.res.PickHistoryResponse;
import com.example.kpick.benefit.repository.AdRewardHistoryRepository;
import com.example.kpick.benefit.repository.AttendanceCheckRepository;
import com.example.kpick.benefit.repository.BenefitSettingRepository;
import com.example.kpick.benefit.repository.PickHistoryRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.notification.domain.PushNotificationType;
import com.example.kpick.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitService {
    private final BenefitSettingRepository benefitSettingRepository;
    private final AttendanceCheckRepository attendanceCheckRepository;
    private final AdRewardHistoryRepository adRewardHistoryRepository;
    private final PickHistoryRepository pickHistoryRepository;
    private final ProfileRepository profileRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional(readOnly = true)
    public BenefitHomeResponse getBenefitHome(Long profileId) {
        findProfile(profileId);
        BenefitSetting setting = getSetting();
        LocalDate today = LocalDate.now();
        int watchedCount = (int) adRewardHistoryRepository.countByProfileIdAndRewardDate(profileId, today);

        return new BenefitHomeResponse(
                profileId,
                new BenefitHomeResponse.AttendanceBenefitResponse(
                        calculateConsecutiveAttendanceDays(profileId, today),
                        setting.getDailyAttendancePick(),
                        setting.getWeeklyAttendanceBonusPick(),
                        attendanceCheckRepository.existsByProfileIdAndAttendanceDate(profileId, today),
                        createWeekAttendance(profileId, setting, today)
                ),
                new BenefitHomeResponse.AdBenefitResponse(
                        setting.getDailyAdLimit(),
                        watchedCount,
                        Math.max(setting.getDailyAdLimit() - watchedCount, 0),
                        setting.getAdRewardPick()
                ),
                List.of(
                        new BenefitHomeResponse.MiniGameResponse("OX_QUIZ", "OX 퀴즈", "COMING_SOON"),
                        new BenefitHomeResponse.MiniGameResponse("ROULETTE", "룰렛", "COMING_SOON"),
                        new BenefitHomeResponse.MiniGameResponse("CARD_FLIP", "카드 뒤집기", "COMING_SOON")
                )
        );
    }

    @Transactional
    public AttendanceCheckResponse checkAttendance(Long profileId) {
        Profile profile = findProfile(profileId);
        BenefitSetting setting = getOrCreateSetting();
        LocalDate today = LocalDate.now();
        if (attendanceCheckRepository.existsByProfileIdAndAttendanceDate(profileId, today)) {
            throw new IllegalArgumentException("Already checked attendance today.");
        }

        attendanceCheckRepository.save(AttendanceCheck.create(profileId, today));
        int consecutiveDays = calculateConsecutiveAttendanceDays(profileId, today);
        boolean weeklyBonusEarned = consecutiveDays > 0 && consecutiveDays % 7 == 0;
        long earnedPick = setting.getDailyAttendancePick()
                + (weeklyBonusEarned ? setting.getWeeklyAttendanceBonusPick() : 0L);
        profile.addCoin(earnedPick);
        pickHistoryRepository.save(PickHistory.create(profileId, profile.getNickname(), PickHistoryType.ATTENDANCE,
                setting.getDailyAttendancePick(), "출석체크"));
        if (weeklyBonusEarned) {
            pickHistoryRepository.save(PickHistory.create(profileId, profile.getNickname(), PickHistoryType.ATTENDANCE_BONUS,
                    setting.getWeeklyAttendanceBonusPick(), "7일 개근 보너스"));
        }
        notifyPointReward(profileId, earnedPick, "출석체크");

        return new AttendanceCheckResponse(
                profileId,
                today,
                earnedPick,
                consecutiveDays,
                weeklyBonusEarned,
                profile.getCoin() == null ? 0L : profile.getCoin(),
                createWeekAttendance(profileId, setting, today)
        );
    }

    @Transactional
    public AdRewardResponse rewardAd(Long profileId) {
        Profile profile = findProfile(profileId);
        BenefitSetting setting = getOrCreateSetting();
        LocalDate today = LocalDate.now();
        int watchedCount = (int) adRewardHistoryRepository.countByProfileIdAndRewardDate(profileId, today);
        if (watchedCount >= setting.getDailyAdLimit()) {
            throw new IllegalArgumentException("Daily ad reward limit exceeded.");
        }

        adRewardHistoryRepository.save(AdRewardHistory.create(profileId, today));
        profile.addCoin(setting.getAdRewardPick());
        pickHistoryRepository.save(PickHistory.create(profileId, profile.getNickname(), PickHistoryType.AD_REWARD,
                setting.getAdRewardPick(), "광고 시청"));
        notifyPointReward(profileId, setting.getAdRewardPick(), "광고 시청");

        int updatedWatchedCount = watchedCount + 1;
        return new AdRewardResponse(
                profileId,
                today,
                setting.getAdRewardPick(),
                updatedWatchedCount,
                Math.max(setting.getDailyAdLimit() - updatedWatchedCount, 0),
                profile.getCoin() == null ? 0L : profile.getCoin()
        );
    }

    @Transactional(readOnly = true)
    public BenefitSettingResponse getBenefitSetting() {
        return BenefitSettingResponse.from(getSetting());
    }

    @Transactional
    public BenefitSettingResponse updateBenefitSetting(UpdateBenefitSettingRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        validateBenefitSettingRequest(request);
        BenefitSetting setting = getOrCreateSetting();
        setting.update(request);
        return BenefitSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public PickHistoryResponse getPickHistories(PickHistoryType pickHistoryType, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        List<PickHistory> histories = pickHistoryType == null
                ? pickHistoryRepository.findAllByOrderByCreatedAtDesc()
                : pickHistoryRepository.findByPickHistoryTypeOrderByCreatedAtDesc(pickHistoryType);
        List<PickHistoryResponse.PickHistoryItemResponse> items = histories.stream()
                .filter(history -> fromDate == null || !history.getCreatedAt().toLocalDate().isBefore(fromDate))
                .filter(history -> toDate == null || !history.getCreatedAt().toLocalDate().isAfter(toDate))
                .map(PickHistoryResponse.PickHistoryItemResponse::from)
                .toList();
        return new PickHistoryResponse(pickHistoryType, items.size(), items);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate cannot be before fromDate.");
        }
    }

    private BenefitSetting getSetting() {
        return benefitSettingRepository.findAll().stream()
                .findFirst()
                .orElseGet(BenefitSetting::defaultSetting);
    }

    private void notifyPointReward(Long profileId, long earnedPick, String reason) {
        pushNotificationService.notify(
                profileId,
                PushNotificationType.POINT_REWARD,
                "Pick이 지급되었어요",
                reason + " 보상으로 " + earnedPick + " Pick을 받았어요.",
                "BENEFIT",
                null
        );
    }

    private BenefitSetting getOrCreateSetting() {
        return benefitSettingRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> benefitSettingRepository.save(BenefitSetting.defaultSetting()));
    }

    private List<BenefitHomeResponse.AttendanceDayResponse> createWeekAttendance(Long profileId, BenefitSetting setting, LocalDate today) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<LocalDate> checkedDates = attendanceCheckRepository.findByProfileIdAndAttendanceDateBetween(profileId, monday, sunday).stream()
                .map(AttendanceCheck::getAttendanceDate)
                .toList();

        return monday.datesUntil(sunday.plusDays(1))
                .map(date -> new BenefitHomeResponse.AttendanceDayResponse(
                        date,
                        date.getDayOfWeek().name(),
                        date.getDayOfWeek() == DayOfWeek.SUNDAY
                                ? setting.getWeeklyAttendanceBonusPick()
                                : setting.getDailyAttendancePick(),
                        checkedDates.contains(date),
                        date.equals(today),
                        date.getDayOfWeek() == DayOfWeek.SUNDAY
                ))
                .toList();
    }

    private int calculateConsecutiveAttendanceDays(Long profileId, LocalDate today) {
        List<LocalDate> attendanceDates = attendanceCheckRepository
                .findByProfileIdAndAttendanceDateLessThanEqualOrderByAttendanceDateDesc(profileId, today)
                .stream()
                .map(AttendanceCheck::getAttendanceDate)
                .toList();
        int consecutiveDays = 0;
        LocalDate expectedDate = today;
        for (LocalDate attendanceDate : attendanceDates) {
            if (!attendanceDate.equals(expectedDate)) {
                break;
            }
            consecutiveDays++;
            expectedDate = expectedDate.minusDays(1);
        }
        return consecutiveDays;
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private void validateBenefitSettingRequest(UpdateBenefitSettingRequest request) {
        validateZeroOrPositive(request.getDailyAttendancePick(), "dailyAttendancePick");
        validateZeroOrPositive(request.getWeeklyAttendanceBonusPick(), "weeklyAttendanceBonusPick");
        validateZeroOrPositive(request.getDailyAdLimit(), "dailyAdLimit");
        validateZeroOrPositive(request.getAdRewardPick(), "adRewardPick");
    }

    private void validateZeroOrPositive(Integer value, String fieldName) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or positive.");
        }
    }
}
