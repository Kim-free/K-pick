package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionAttender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionConfirmModalResponse {
    private Long missionId;
    private Long missionAttenderId;
    private Long selectedMissionOptionId;
    private String selectedOptionContent;
    private int paidCoin;
    private Long remainingCoin;
    private boolean resultNotificationEnabled;

    public static MissionConfirmModalResponse from(Mission mission, MissionAttender missionAttender, Long remainingCoin) {
        return new MissionConfirmModalResponse(
                mission.getId(),
                missionAttender.getId(),
                missionAttender.getMissionOption().getId(),
                missionAttender.getMissionOption().getContent(),
                mission.getCoinFee(),
                remainingCoin,
                true
        );
    }
}
