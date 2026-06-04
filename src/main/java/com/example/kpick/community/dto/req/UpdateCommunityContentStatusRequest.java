package com.example.kpick.community.dto.req;

import com.example.kpick.community.domain.CommunityContentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommunityContentStatusRequest {
    private CommunityContentStatus contentStatus;
}
