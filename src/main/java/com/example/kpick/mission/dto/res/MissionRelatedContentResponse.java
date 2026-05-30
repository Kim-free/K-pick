package com.example.kpick.mission.dto.res;

import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionRelatedContentResponse {
    private RelatedMissionResponse relatedMission;
    private RelatedThreadResponse relatedThread;

    public static MissionRelatedContentResponse from(RelatedMissionResponse relatedMission, RelatedThreadResponse relatedThread) {
        return new MissionRelatedContentResponse(relatedMission, relatedThread);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedMissionResponse {
        private Long missionId;
        private String programName;
        private String season;
        private String missionName;
        private int coinFee;
        private LocalDateTime dueDateTime;

        public static RelatedMissionResponse from(Mission mission, Program program) {
            return new RelatedMissionResponse(
                    mission.getId(),
                    program.getProgramName(),
                    program.getSeason(),
                    mission.getMissionName(),
                    mission.getCoinFee(),
                    mission.getDueDateTime()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedThreadResponse {
        private Long threadId;
        private String programName;
        private String title;
        private String description;

        public static RelatedThreadResponse from(Thread thread, Program program) {
            return new RelatedThreadResponse(
                    thread.getId(),
                    program.getProgramName(),
                    thread.getTitle(),
                    thread.getDescription()
            );
        }
    }
}
