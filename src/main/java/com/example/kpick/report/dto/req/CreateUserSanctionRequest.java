package com.example.kpick.report.dto.req;

import com.example.kpick.report.domain.UserSanctionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserSanctionRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long createdByProfileId;
    private Long relatedReportId;
    private UserSanctionType sanctionType;
    private String reason;
    private Boolean notifyUser;

    public CreateUserSanctionRequest withCreatedByProfileId(Long createdByProfileId) {
        return new CreateUserSanctionRequest(createdByProfileId, relatedReportId, sanctionType, reason, notifyUser);
    }
}
