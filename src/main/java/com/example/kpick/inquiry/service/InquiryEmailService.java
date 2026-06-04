package com.example.kpick.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryEmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public boolean sendInquiryReply(String replyEmail, String inquiryTitle, String answerContent) {
        if (mailHost == null || mailHost.isBlank()) {
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderEmail);
        }
        message.setTo(replyEmail);
        message.setSubject("[PICKTORI] 문의 답변: " + inquiryTitle);
        message.setText(answerContent);
        mailSender.send(message);
        return true;
    }
}
