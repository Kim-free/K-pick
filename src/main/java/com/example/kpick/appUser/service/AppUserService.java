package com.example.kpick.appUser.service;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;

    @Transactional
    public void withdraw(Long appUserId) {
        if (appUserId == null) {
            throw new IllegalArgumentException("appUserId is required.");
        }
        AppUser appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found. appUserId=" + appUserId));
        appUser.withdraw();
    }
}
