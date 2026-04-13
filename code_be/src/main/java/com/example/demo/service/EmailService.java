package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String resetLink) {

        if (mailSender == null) {
            System.out.println("Skip sending mail (no mailSender)");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String html = "<p>We received a request to reset your password.</p>"
                    + "<p>Click the link below to reset your password (valid for 1 hour):</p>"
                    + "<p><a href='" + resetLink + "'>Reset password</a></p>"
                    + "<p>If you didn't request this, please ignore this email.</p>";

            helper.setText(html, true);
            helper.setTo(to);
            helper.setFrom("WebDocTruyen <" + fromEmail + ">");
            helper.setSubject("[WebDocTruyen] Reset your password");

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}