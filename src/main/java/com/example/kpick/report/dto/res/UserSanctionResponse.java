package com.example.kpick.report.dto.res;

import com.example.kpick.report.domain.UserSanction;
import com.example.kpick.report.domain.UserSanctionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSanctionResponse {
    private Long userSanctionId;
    private Long profileId;
    private Long relatedReportId;
    private UserSanctionType sanctionType;
    private String reason;
    private Boolean notifyUser;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    public static UserSanctionResponse from(UserSanction sanction) {
        return new UserSanctionResponse(
                sanction.getId(),
                sanction.getProfileId(),
                sanction.getRelatedReportId(),
                sanction.getSanctionType(),
                sanction.getReason(),
                sanction.getNotifyUser(),
                sanction.getStartsAt(),
                sanction.getEndsAt()
        );
    }
}
