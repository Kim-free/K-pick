package com.example.kpick.image.controller;

import com.example.kpick.image.dto.req.CreatePresignedUrlRequest;
import com.example.kpick.image.dto.res.PresignedUrlResponse;
import com.example.kpick.image.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> createPresignedUrl(@RequestBody CreatePresignedUrlRequest request) {
        return ResponseEntity.ok(imageUploadService.createPresignedUrl(request));
    }
}
