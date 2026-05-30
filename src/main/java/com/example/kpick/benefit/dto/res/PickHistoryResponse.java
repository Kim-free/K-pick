package com.example.kpick.benefit.dto.res;

import com.example.kpick.benefit.domain.PickHistory;
import com.example.kpick.benefit.domain.PickHistoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PickHistoryResponse {
    private PickHistoryType pickHistoryType;
    private long totalCount;
    private List<PickHistoryItemResponse> histories;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickHistoryItemResponse {
        private Long pickHistoryId;
        private Long profileId;
        private String nickname;
        private PickHistoryType pickHistoryType;
        private long amount;
        private String description;
        private LocalDateTime createdAt;

        public static PickHistoryItemResponse from(PickHistory history) {
            return new PickHistoryItemResponse(
                    history.getId(),
                    history.getProfileId(),
                    history.getNickname(),
                    history.getPickHistoryType(),
                    history.getAmount(),
                    history.getDescription(),
                    history.getCreatedAt()
            );
        }
    }
}
