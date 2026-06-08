package com.example.kpick.appUser.controller;

import com.example.kpick.appUser.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app-users")
public class AppUserController {
    private final AppUserService appUserService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @RequestAttribute("authenticatedAppUserId") Long appUserId
    ) {
        appUserService.withdraw(appUserId);
        return ResponseEntity.noContent().build();
    }
}
