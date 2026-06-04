package com.example.kpick.community.thread.domain;

import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.thread.dto.req.CreateThreadRequest;
import com.example.kpick.community.thread.dto.req.UpdateThreadRequest;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class Thread extends CommunityPost {
    private Long missionId;
    private String description;
    private boolean isNicknamePublic;

    @ElementCollection
    @CollectionTable(name = "thread_image_url", joinColumns = @JoinColumn(name = "thread_id"))
    private List<String> imageUrls;

    public static Thread toEntity(CreateThreadRequest request, Long resolvedProgramId) {
        Thread thread = Thread.builder()
                .missionId(request.getMissionId())
                .description(request.getDescription().trim())
                .isNicknamePublic(Boolean.TRUE.equals(request.getIsNicknamePublic()))
                .imageUrls(request.getImageUrls() == null ? List.of() : List.copyOf(request.getImageUrls()))
                .build();
        thread.initializeCommunityPost(request.getProfileId(), resolvedProgramId, request.getTitle());
        return thread;
    }

    public void update(UpdateThreadRequest request) {
        if (request.getProgramId() != null) {
            this.programId = request.getProgramId();
        }
        if (request.getTitle() != null) {
            this.title = request.getTitle().trim();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription().trim();
        }
        if (request.getIsNicknamePublic() != null) {
            this.isNicknamePublic = request.getIsNicknamePublic();
        }
        if (request.getImageUrls() != null) {
            this.imageUrls = List.copyOf(request.getImageUrls());
        }
    }
}
