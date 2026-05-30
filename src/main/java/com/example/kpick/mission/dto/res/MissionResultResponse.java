package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionAttender;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionResultResponse {
    private Long missionId;
    private Long programId;
    private String programName;
    private String missionName;
    private MissionState missionState;
    private boolean resultConfirmed;
    private int totalAttenderCount;
    private Long correctMissionOptionId;
    private String correctOptionContent;
    private Long selectedMissionOptionId;
    private String selectedOptionContent;
    private Boolean isCorrect;
    private Integer earnedPoint;
    private List<OptionResultResponse> optionResults;

    public static MissionResultResponse from(
            Mission mission,
            Program program,
            MissionOption correctOption,
            MissionAttender missionAttender,
            Integer earnedPoint,
            List<OptionResultResponse> optionResults
    ) {
        MissionOption selectedOption = missionAttender == null ? null : missionAttender.getMissionOption();
        Boolean isCorrect = selectedOption == null || correctOption == null ? null : selectedOption.getId().equals(correctOption.getId());

        return new MissionResultResponse(
                mission.getId(),
                mission.getProgramId(),
                program.getProgramName(),
                mission.getMissionName(),
                mission.getMissionState(),
                true,
                mission.getAttenderCount(),
                correctOption == null ? null : correctOption.getId(),
                correctOption == null ? null : correctOption.getContent(),
                selectedOption == null ? null : selectedOption.getId(),
                selectedOption == null ? null : selectedOption.getContent(),
                isCorrect,
                earnedPoint,
                optionResults
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResultResponse {
        private Long missionOptionId;
        private String content;
        private Integer displayOrder;
        private long selectedCount;
        private int selectedRate;

        public static OptionResultResponse from(MissionOption option, long selectedCount, long totalAttenderCount) {
            int selectedRate = totalAttenderCount == 0 ? 0 : (int) Math.round((selectedCount * 100.0) / totalAttenderCount);
            return new OptionResultResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder(),
                    selectedCount,
                    selectedRate
            );
        }
    }
}
