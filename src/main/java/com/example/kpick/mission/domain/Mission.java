package com.example.kpick.mission.domain;

import com.example.kpick.mission.dto.req.CreateMissionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long programId;
    private Long profileId;

    private String missionName;
    private String episode;
    private LocalDateTime dueDateTime;
    private int coinFee;
    private int attenderCount;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private ResultPublishTiming resultPublishTiming;

    @Enumerated(EnumType.STRING)
    private MissionState missionState;

    public void complete(ResultPublishTiming resultPublishTiming) {
        this.resultPublishTiming = resultPublishTiming == null ? ResultPublishTiming.IMMEDIATE : resultPublishTiming;
        this.missionState = MissionState.COMPLETED;
    }

    public void increaseAttenderCount() {
        this.attenderCount++;
    }

    public static Mission toEntity(CreateMissionRequest request){
        return Mission.builder()
                .programId(request.getProgramId())
                .profileId(request.getProfileId())
                .missionName(request.getMissionName().trim())
                .episode(request.getEpisode() == null ? null : request.getEpisode().trim())
                .dueDateTime(request.getDueDateTime())
                .coinFee(request.getCoinFee())
                .attenderCount(0)
                .isActive(request.getIsActive() == null || request.getIsActive())
                .resultPublishTiming(ResultPublishTiming.IMMEDIATE)
                .missionState(MissionState.ONGOING)
                .build();
    }
}
