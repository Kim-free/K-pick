package com.example.kpick.profile.domain;

import com.example.kpick.program.domain.ProgramInterest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appUserId;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ProgramInterest> programInterests;

    private String nickname;
    private String profileImageUrl;
    private boolean gender;
    @Enumerated(EnumType.STRING)
    private ProfileGender profileGender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthDate;
    private Long coin = 100L;
    private Long missionPoint;
    private Long totalMissionPoint;
    private Long activityPoint;
    private String inviteCode;
    private Long invitedByProfileId;
    private String joinPath;

    @Builder.Default
    private SignUpStatus signUpStatus = SignUpStatus.NULL;

    public void deductCoin(int amount) {
        long currentCoin = this.coin == null ? 0L : this.coin;
        if (currentCoin < amount) {
            throw new IllegalArgumentException("Not enough coin.");
        }
        this.coin = currentCoin - amount;
    }

    public void addCoin(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be zero or positive.");
        }
        long currentCoin = this.coin == null ? 0L : this.coin;
        this.coin = currentCoin + amount;
    }

    public void addMissionPoint(long point) {
        long currentPoint = this.missionPoint == null ? 0L : this.missionPoint;
        long currentTotalPoint = getTotalMissionPointValue();
        this.missionPoint = currentPoint + point;
        this.totalMissionPoint = currentTotalPoint + point;
    }

    public void addActivityPoint(long point) {
        long currentPoint = this.activityPoint == null ? 0L : this.activityPoint;
        this.activityPoint = currentPoint + point;
    }

    public long getMissionPointValue() {
        return this.missionPoint == null ? 0L : this.missionPoint;
    }

    public long getTotalMissionPointValue() {
        return this.totalMissionPoint == null ? getMissionPointValue() : this.totalMissionPoint;
    }

    public long getActivityPointValue() {
        return this.activityPoint == null ? 0L : this.activityPoint;
    }

    public void applySeasonPointDecay() {
        long currentPoint = getMissionPointValue();
        long decayedPoint = (long) Math.floor(currentPoint * 0.7);
        this.missionPoint = currentPoint >= 100 ? Math.max(decayedPoint, 100) : decayedPoint;
    }

    public void assignInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("inviteCode is required.");
        }
        this.inviteCode = inviteCode;
    }

    public void updateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname is required.");
        }
        String trimmedNickname = nickname.trim();
        if (trimmedNickname.length() < 2 || trimmedNickname.length() > 12) {
            throw new IllegalArgumentException("nickname must be 2 to 12 characters.");
        }
        this.nickname = trimmedNickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            throw new IllegalArgumentException("profileImageUrl is required.");
        }
        this.profileImageUrl = profileImageUrl.trim();
    }

    public void completeOnboarding(
            String nickname,
            String profileImageUrl,
            ProfileGender gender,
            LocalDate birthDate,
            String joinPath
    ) {
        updateNickname(nickname);
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl.trim();
        }
        if (gender == null) {
            throw new IllegalArgumentException("gender is required.");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("birthDate is required.");
        }
        this.profileGender = gender;
        this.gender = gender == ProfileGender.MALE;
        this.birthDate = birthDate;
        this.joinPath = joinPath == null || joinPath.isBlank() ? null : joinPath.trim();
        this.signUpStatus = SignUpStatus.NICKNAMEDONE;
    }

    public boolean hasReceivedInviteReward() {
        return this.invitedByProfileId != null;
    }

    public void markInvitedBy(Long inviterProfileId) {
        if (inviterProfileId == null) {
            throw new IllegalArgumentException("inviterProfileId is required.");
        }
        this.invitedByProfileId = inviterProfileId;
    }

    @PrePersist
    private void initializeInviteCode() {
        if (this.inviteCode == null || this.inviteCode.isBlank()) {
            this.inviteCode = generateInviteCode();
        }
    }

    private String generateInviteCode() {
        return randomLetters(5) + "-" + randomLetters(2) + randomDigits(2);
    }

    private String randomLetters(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) ('A' + ThreadLocalRandom.current().nextInt(26)));
        }
        return builder.toString();
    }

    private String randomDigits(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }

    private void checkSignUpStatus(){
        if(programInterests != null && !programInterests.isEmpty()){
            this.signUpStatus = SignUpStatus.COMPLETE;
        }else if(nickname != null && !nickname.isEmpty()){
            this.signUpStatus = SignUpStatus.NICKNAMEDONE;
        }
    }
}
