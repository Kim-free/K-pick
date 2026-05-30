package com.example.kpick.inquiry.repository;

import com.example.kpick.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
