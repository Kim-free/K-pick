package com.example.kpick.community.thread.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadRequest {
    private Long profileId;
    private Long programId;
    private Long missionId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
}
