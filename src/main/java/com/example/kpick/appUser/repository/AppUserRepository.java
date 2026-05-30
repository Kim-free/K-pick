package com.example.kpick.appUser.repository;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.domain.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginTypeAndProviderId(LoginType loginType, String providerId);
}
