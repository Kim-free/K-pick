package com.example.kpick.profile.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionHistoryResponse {
    private Long profileId;
    private String resultFilter;
    private long totalCount;
    private List<MissionHistoryItemResponse> missions;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionHistoryItemResponse {
        private Long missionId;
        private Long programId;
        private String programName;
        private String episode;
        private String missionName;
        private Long selectedMissionOptionId;
        private String selectedOptionContent;
        private Long correctMissionOptionId;
        private String correctOptionContent;
        private String missionResultStatus;
        private int coinFee;
        private long earnedPoint;
        private LocalDateTime dueDateTime;
    }
}
