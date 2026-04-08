package com.example.demo.controller;
import com.example.demo.entity.Chapter;
import com.example.demo.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class ReaderController {
    private final ChapterService chapterService;
    private final UserRepository userRepository;

    // Đổi từ /reader thành /read để không trùng với ChapterController
    @GetMapping("/read/{chapterId}")
    public String readChapter(@PathVariable Long chapterId, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Chapter chapter = chapterService.getChapterById(chapterId);
        model.addAttribute("chapter", chapter);
        model.addAttribute("story", chapter.getStory());

        // Giữ nguyên logic xử lý ReadingProgress của bạn...

        if (chapter.getType() == Chapter.ChapterType.COMIC) {
            return "reader/comic-reader";
        }
        return "reader/text-reader";
    }
}