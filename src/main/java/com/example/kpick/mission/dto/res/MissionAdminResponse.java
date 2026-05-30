package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.domain.ResultPublishTiming;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionAdminResponse {
    private Long missionId;
    private Long programId;
    private String programName;
    private String episode;
    private String missionName;
    private MissionState missionState;
    private int attenderCount;
    private LocalDateTime dueDateTime;
    private int coinFee;
    private ResultPublishTiming resultPublishTiming;
    private boolean resultConfirmable;
    private List<OptionResponse> options;

    public static MissionAdminResponse from(
            Mission mission,
            Program program,
            List<MissionOption> options,
            Map<Long, Long> optionSelectionCounts
    ) {
        return new MissionAdminResponse(
                mission.getId(),
                mission.getProgramId(),
                program == null ? null : program.getProgramName(),
                mission.getEpisode(),
                mission.getMissionName(),
                mission.getMissionState(),
                mission.getAttenderCount(),
                mission.getDueDateTime(),
                mission.getCoinFee(),
                mission.getResultPublishTiming(),
                mission.getMissionState() != MissionState.COMPLETED,
                options.stream()
                        .map(option -> OptionResponse.from(option, mission.getAttenderCount(), optionSelectionCounts.getOrDefault(option.getId(), 0L)))
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResponse {
        private Long missionOptionId;
        private String content;
        private Integer displayOrder;
        private long selectedCount;
        private int selectedRate;
        private Boolean isCorrect;

        public static OptionResponse from(MissionOption option, int totalAttenderCount, long selectedCount) {
            int selectedRate = totalAttenderCount == 0 ? 0 : (int) Math.round((selectedCount * 100.0) / totalAttenderCount);
            return new OptionResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder(),
                    selectedCount,
                    selectedRate,
                    option.getIsCorrect()
            );
        }
    }
}
