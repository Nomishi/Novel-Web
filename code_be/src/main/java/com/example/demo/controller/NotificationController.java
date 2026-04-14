package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /* trang danh sách thông báo của người dùng hiện tại*/
    @GetMapping
    public String listNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        //ID truyền vào Repository
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //danh sách thông báo theo UserId sắp xếp mới nhất lên đầu
        List<Notification> notifications = notificationRepository.findByUserIdOrderByIsReadAscCreatedAtDesc(user.getId());
        model.addAttribute("notifications", notifications);
        return "user/notifications";
    }

    @PostMapping("/read/{id}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return notificationRepository.findById(id).map(notification -> {
            // Kiểm tra bảo mật: Chỉ chủ sở hữu thông báo mới được đánh dấu đọc
            if (notification.getUser().getUsername().equals(userDetails.getUsername())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
                return ResponseEntity.ok().build(); // Trả về 200 OK
            }
            return ResponseEntity.status(403).build(); // Không có quyền
        }).orElse(ResponseEntity.notFound().build());
    }
}