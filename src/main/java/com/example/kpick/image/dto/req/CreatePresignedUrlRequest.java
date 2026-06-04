package com.example.kpick.image.dto.req;

import com.example.kpick.image.domain.ImageUploadPurpose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePresignedUrlRequest {
    private ImageUploadPurpose uploadPurpose;
    private String fileName;
    private String contentType;
    private Long fileSize;
}
