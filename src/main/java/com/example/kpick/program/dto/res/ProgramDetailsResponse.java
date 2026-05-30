package com.example.kpick.program.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDetailsResponse {
    private Long programId;
    private String programName;
    private String broadcaster;
    private Genre genre;
    private String season;
    private int episodeCount;
    private LocalDate broadcastStartDate;
    private LocalDate broadcastEndDate;
    private String thumbnailImageUrl;
    private Boolean isOnAir;
    private Boolean isExposed;
    private String description;
    private int missionCount;
    private List<ConnectedMissionResponse> missions;

    public static ProgramDetailsResponse from(Program program, List<Mission> missions) {
        return new ProgramDetailsResponse(
                program.getId(),
                program.getProgramName(),
                program.getBroadcaster(),
                program.getGenre(),
                program.getSeason(),
                program.getEpisodeCount(),
                program.getBroadcastStartDate(),
                program.getBroadcastEndDate(),
                program.getThumbnailImageUrl(),
                program.isOnAir(),
                program.isExposed(),
                program.getDescription(),
                missions.size(),
                missions.stream()
                        .map(ConnectedMissionResponse::from)
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectedMissionResponse {
        private Long missionId;
        private String missionName;
        private LocalDateTime dueDateTime;
        private int attenderCount;
        private MissionState missionState;

        public static ConnectedMissionResponse from(Mission mission) {
            return new ConnectedMissionResponse(
                    mission.getId(),
                    mission.getMissionName(),
                    mission.getDueDateTime(),
                    mission.getAttenderCount(),
                    mission.getMissionState()
            );
        }
    }
}
