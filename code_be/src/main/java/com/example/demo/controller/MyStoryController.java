package com.example.demo.controller;

import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.service.StoryUploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/uploader")
@RequiredArgsConstructor
public class MyStoryController {
    private final StoryUploaderService uploaderService;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    @GetMapping("/stories")
    public String listMyStories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("myStories", uploaderService.getStoriesByUploader(user));
        return "uploader/story-list";
    }

    // Đổi thành /upload-story để không trùng với bất kỳ file nào khác
    @GetMapping("/stories/upload-story")
    public String showUploadForm(Model model) {
        model.addAttribute("story", new Story());
        return "uploader/story-form";
    }

    @PostMapping("/stories/upload-story")
    public String processUpload(@ModelAttribute Story story,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        uploaderService.uploadNewStory(story, user);
        return "redirect:/user/bookshelf";
    }

    @PostMapping("/stories/delete/{id}")
    public String deleteMyStory(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            uploaderService.deleteByOwner(id, user);
            ra.addFlashAttribute("success", "Xóa truyện thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/bookshelf";
    }

    @GetMapping("/stories/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // Kiểm tra quyền chủ sở hữu trước khi cho phép sửa
        if (!uploaderService.isOwner(id, user)) {
            return "redirect:/user/bookshelf?error=denied";
        }
        Story story = storyRepository.findById(id).orElseThrow();
        model.addAttribute("story", story);
        model.addAttribute("isEdit", true);
        return "uploader/story-form";
    }

    @PostMapping("/stories/edit/{id}")
    public String processUpdate(@PathVariable Long id,
                                @ModelAttribute("story") Story storyData,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        try {
            // Bạn cần thêm hàm updateStoryInfo này vào StoryUploaderService
            uploaderService.updateStoryByOwner(id, storyData, user);
            ra.addFlashAttribute("success", "Cập nhật thông tin truyện thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/user/bookshelf";
    }

    
}