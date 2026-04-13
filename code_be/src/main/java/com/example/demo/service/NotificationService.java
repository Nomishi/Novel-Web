package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.repository.UserRepository; // Cần thêm để lấy ID từ username
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository; // Tiêm thêm để xử lý logic theo Username

    public void notifyNewChapter(Chapter chapter) {
        // Chỉ gửi thông báo cho những người bật tính năng nhận thông báo
        List<Bookshelf> followers = bookshelfRepository.findByStoryIdAndNotifyOnNewChapterTrue(chapter.getStory().getId());
        for (Bookshelf follow : followers) {
            Notification notif = Notification.builder()
                    .user(follow.getUser())
                    .content("Truyện [" + chapter.getStory().getTitle() + "] vừa ra chương mới: " + chapter.getChapterNumber())
                    .url("/reader/" + chapter.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }
    }

    public long countUnread(String username) {
        //lấy User từ username trước để có ID
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}