package com.example.demo.controller;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.service.ChapterService;
import com.example.demo.service.StoryUploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/uploader/manage")
@RequiredArgsConstructor
public class ChapterUploaderController {
    private final StoryUploaderService uploaderService;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final ChapterService chapterService;

    @GetMapping("/stories/{storyId}/chapters/add")
    public String showAddChapterForm(@PathVariable Long storyId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model,
                                     RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!uploaderService.isOwner(storyId, user)) {
            return "redirect:/error?msg=unauthorized";
        }

        Story story = storyRepository.findById(storyId).orElseThrow();
        if (story.getStatus() == Story.StoryStatus.LOCKED) {
            ra.addFlashAttribute("error", "Truyện này đã bị khóa. Bạn không thể thêm chương mới!");
            return "redirect:/uploader/manage/stories/" + storyId + "/chapters/manage";
        }

        model.addAttribute("chapter", new Chapter());
        model.addAttribute("storyId", storyId);

        return "uploader/chapter-form";
    }

    @PostMapping("/stories/{storyId}/chapters/save")
    public String processAddChapter(@PathVariable Long storyId,
                                    @ModelAttribute Chapter chapter,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Story story = storyRepository.findById(storyId).orElseThrow();
        if (story.getStatus() == Story.StoryStatus.LOCKED) {
            ra.addFlashAttribute("error", "Thao tác thất bại! Truyện đã bị khóa.");
            return "redirect:/uploader/manage/stories/" + storyId + "/chapters/manage";
        }

        try {
            uploaderService.addChapterByOwner(storyId, chapter, user);
            ra.addFlashAttribute("success", "Đăng chương mới thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/uploader/manage/stories/" + storyId + "/chapters/manage";
    }

    @GetMapping("/stories/{storyId}/chapters/manage")
    public String manageChapters(@PathVariable Long storyId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!uploaderService.isOwner(storyId, user)) {
            return "redirect:/user/bookshelf?error=denied";
        }

        // 2. Lấy dữ liệu truyện và danh sách chương
        Story story = storyRepository.findById(storyId).orElseThrow();
        model.addAttribute("story", story);
        model.addAttribute("chapters", chapterService.getChaptersByStoryId(storyId));
        model.addAttribute("storyId", storyId);

        return "uploader/chapter-manage";
    }

    @GetMapping("/stories/{storyId}/chapters/edit/{chapterId}")
    public String showEditChapterForm(@PathVariable Long storyId,
                                      @PathVariable Long chapterId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (!uploaderService.isOwner(storyId, user)) {
            return "redirect:/user/bookshelf?error=denied";
        }

        //Lấy dữ liệu chương cũ
        Chapter chapter = chapterService.getChapterById(chapterId);
        model.addAttribute("chapter", chapter);
        model.addAttribute("storyId", storyId);
        model.addAttribute("isEdit", true); // Biến để nhận biết đang sửa

        return "uploader/chapter-form";
    }

    @PostMapping("/stories/{storyId}/chapters/edit/{chapterId}")
    public String processUpdateChapter(@PathVariable Long storyId,
                                       @PathVariable Long chapterId,
                                       @ModelAttribute Chapter chapterData,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            uploaderService.updateChapterByOwner(chapterId, chapterData, user);
            ra.addFlashAttribute("success", "Cập nhật chương thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/uploader/manage/stories/" + storyId + "/chapters/manage";
    }

    @PostMapping("/stories/{storyId}/chapters/delete/{chapterId}")
    public String deleteChapter(@PathVariable Long storyId,
                                @PathVariable Long chapterId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!uploaderService.isOwner(storyId, user)) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa chương này!");
            return "redirect:/user/bookshelf";
        }

        try {
            chapterService.deleteChapter(chapterId);
            ra.addFlashAttribute("success", "Xóa chương thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/uploader/manage/stories/" + storyId + "/chapters/manage";
    }
}