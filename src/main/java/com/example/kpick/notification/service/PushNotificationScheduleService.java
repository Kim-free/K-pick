package com.example.kpick.notification.service;

import com.example.kpick.benefit.repository.AttendanceCheckRepository;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.notification.domain.PushNotificationType;
import com.example.kpick.notification.repository.PushNotificationRepository;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.repository.ProgramInterestRepository;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PushNotificationScheduleService {
    private static final String TARGET_TYPE_MISSION = "MISSION";
    private static final String TARGET_TYPE_ATTENDANCE = "ATTENDANCE";

    private final MissionRepository missionRepository;
    private final MissionAttenderRepository missionAttenderRepository;
    private final ProgramInterestRepository programInterestRepository;
    private final ProgramRepository programRepository;
    private final ProfileRepository profileRepository;
    private final AttendanceCheckRepository attendanceCheckRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    @Scheduled(cron = "${notification.schedule.mission-closing-soon-cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void notifyInterestedMissionClosingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusHours(1).plusMinutes(5);
        missionRepository.findByMissionStateAndDueDateTimeBetweenOrderByDueDateTimeAsc(MissionState.ONGOING, now, until)
                .forEach(this::notifyInterestedProfilesIfNotAttended);
    }

    @Transactional
    @Scheduled(cron = "${notification.schedule.attendance-reminder-cron:0 0 21 * * *}", zone = "Asia/Seoul")
    public void notifyAttendanceReminder() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        profileRepository.findAll().stream()
                .filter(profile -> !attendanceCheckRepository.existsByProfileIdAndAttendanceDate(profile.getId(), today))
                .filter(profile -> !pushNotificationRepository.existsByProfileIdAndNotificationTypeAndTargetTypeAndCreatedAtBetween(
                        profile.getId(),
                        PushNotificationType.ATTENDANCE_REMINDER,
                        TARGET_TYPE_ATTENDANCE,
                        startOfDay,
                        endOfDay
                ))
                .forEach(profile -> pushNotificationService.notify(
                        profile.getId(),
                        PushNotificationType.ATTENDANCE_REMINDER,
                        "출석체크 리마인드",
                        "오늘 출석체크가 아직 안 됐어요.",
                        TARGET_TYPE_ATTENDANCE,
                        null
                ));
    }

    private void notifyInterestedProfilesIfNotAttended(Mission mission) {
        String programName = programRepository.findById(mission.getProgramId())
                .map(Program::getProgramName)
                .orElse("관심 프로그램");
        programInterestRepository.findByProgramId(mission.getProgramId()).stream()
                .filter(interest -> !missionAttenderRepository.existsByMissionIdAndProfileId(mission.getId(), interest.getProfile().getId()))
                .filter(interest -> !pushNotificationRepository.existsSameTargetNotification(
                        interest.getProfile().getId(),
                        PushNotificationType.MISSION_CLOSING_SOON,
                        TARGET_TYPE_MISSION,
                        mission.getId()
                ))
                .forEach(interest -> pushNotificationService.notify(
                        interest.getProfile().getId(),
                        PushNotificationType.MISSION_CLOSING_SOON,
                        "마감 임박",
                        programName + " " + nullToEmpty(mission.getEpisode()).trim() + " 미션이 1시간 후 마감돼요.",
                        TARGET_TYPE_MISSION,
                        mission.getId()
                ));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
