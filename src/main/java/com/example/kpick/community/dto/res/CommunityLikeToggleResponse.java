package com.example.kpick.community.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityLikeToggleResponse {
    private Boolean isLiked;
    private int likeCount;
}
