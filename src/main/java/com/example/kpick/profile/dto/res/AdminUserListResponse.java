package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.AdminUserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {
    private Long profileId;
    private String nickname;
    private String email;
    private String pointTierName;
    private long coin;
    private LocalDateTime joinedAt;
    private AdminUserStatus userStatus;
}
