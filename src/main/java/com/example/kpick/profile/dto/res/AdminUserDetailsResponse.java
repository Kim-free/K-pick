package com.example.kpick.profile.dto.res;

import com.example.kpick.appUser.domain.LoginType;
import com.example.kpick.benefit.dto.res.PickHistoryResponse.PickHistoryItemResponse;
import com.example.kpick.profile.domain.AdminUserStatus;
import com.example.kpick.profile.dto.res.CommunityActivityResponse.CommunityActivityItemResponse;
import com.example.kpick.profile.dto.res.MissionHistoryResponse.MissionHistoryItemResponse;
import com.example.kpick.report.domain.UserSanctionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsResponse {
    private Long profileId;
    private String nickname;
    private String email;
    private LoginType loginType;
    private String pointTierName;
    private long coin;
    private long totalPoint;
    private int correctRate;
    private AdminUserStatus userStatus;
    private UserSanctionType currentSanctionType;
    private LocalDateTime sanctionEndsAt;
    private LocalDateTime joinedAt;
    private LocalDateTime lastLoginAt;
    private List<MissionHistoryItemResponse> missionHistories;
    private List<CommunityActivityItemResponse> communityActivities;
    private List<PickHistoryItemResponse> pickHistories;
}
