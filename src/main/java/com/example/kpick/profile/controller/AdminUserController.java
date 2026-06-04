package com.example.kpick.profile.controller;

import com.example.kpick.profile.domain.AdminUserStatus;
import com.example.kpick.profile.dto.res.AdminUserDetailsResponse;
import com.example.kpick.profile.dto.res.AdminUserListResponse;
import com.example.kpick.profile.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserListResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AdminUserStatus userStatus
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(keyword, userStatus));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<AdminUserDetailsResponse> getUserDetails(@PathVariable Long profileId) {
        return ResponseEntity.ok(adminUserService.getUserDetails(profileId));
    }
}
