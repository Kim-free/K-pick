package com.example.kpick.community.thread.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateThreadRequest {
    private Long programId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
    private List<String> imageUrls;
}
