package com.example.demo.repository;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByExpiryDateBefore(LocalDateTime now);
    void deleteByUser(User user);
}
