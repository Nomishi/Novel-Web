package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.service.EmailService;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered!");
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .displayName(username)
                .readingTimeSeconds(0L)
                .build();
        Role memberRole = roleRepository.findByName("ROLE_MEMBER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_MEMBER")));
        user.getRoles().add(memberRole);
        //CẤP TỰ ĐỘNG ROLE_UPLOADER
        Role uploaderRole = roleRepository.findByName("ROLE_UPLOADER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_UPLOADER")));
        user.getRoles().add(uploaderRole);
        return userRepository.save(user);
    }

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }
        // XÓA TOKEN CŨ (tránh spam nhiều link)
        passwordResetTokenRepository.deleteByUser(user);
        // TẠO TOKEN (hạn 1 giờ)
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(prt);
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        // GỬI MAIL
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Mã đặt lại mật khẩu không hợp lệ."));

        // CHECK HẾT HẠN
        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(prt);
            throw new RuntimeException("Mã đặt lại mật khẩu đã hết hạn.");
        }
        User user = prt.getUser();
        // ENCODE PASSWORD
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // XOÁ TOKEN SAU KHI DÙNG
        passwordResetTokenRepository.delete(prt);
    }

    public void validateResetToken(String token) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ."));

        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(prt);
            throw new RuntimeException("Token đã hết hạn.");
        }
    }
}
