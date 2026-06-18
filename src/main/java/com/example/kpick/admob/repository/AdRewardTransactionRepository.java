package com.example.kpick.admob.repository;

import com.example.kpick.admob.domain.AdRewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRewardTransactionRepository extends JpaRepository<AdRewardTransaction, Long> {
    boolean existsByTransactionId(String transactionId);
}
