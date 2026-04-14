package com.example.demo.controller.api;

import com.example.demo.entity.ReportType;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor

public class ReportController {
    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> payload) {
        if (userDetails == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập");

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        ReportType type = ReportType.valueOf(payload.get("type").toString());
        Long targetId = Long.valueOf(payload.get("targetId").toString());
        String reason = payload.get("reason").toString();
        String content = payload.get("content").toString();
        String result = reportService.sendReport(user, type, targetId, reason, content);

        if (result.contains("thành công")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}