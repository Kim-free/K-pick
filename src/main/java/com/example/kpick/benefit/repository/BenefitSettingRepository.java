package com.example.kpick.benefit.repository;

import com.example.kpick.benefit.domain.BenefitSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitSettingRepository extends JpaRepository<BenefitSetting, Long> {
}
