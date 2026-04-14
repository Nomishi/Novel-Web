package com.example.demo.controller;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryUploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class BookshelfController {
    private final BookshelfRepository bookshelfRepository;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final StoryUploaderService storyUploaderService;

    @GetMapping("/user/bookshelf")
    public String showBookshelf(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("bookshelf", bookshelfRepository.findByUserIdOrderByAddedAtDesc(user.getId()));
        model.addAttribute("progressList", progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
        model.addAttribute("myStories", storyUploaderService.getStoriesByUploader(user));
        return "user/bookshelf";
    }

    @PostMapping("/bookshelf/add")
    public String addToBookshelf(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Story story = storyRepository.findById(storyId).orElseThrow();
        boolean exists = bookshelfRepository.findByUserIdAndStoryId(user.getId(), story.getId()).isPresent();
        if (!exists) {
            bookshelfRepository.save(Bookshelf.builder().user(user).story(story).notifyOnNewChapter(true).build());
        }
        return "redirect:/story/" + story.getSlug();
    }

    @PostMapping("/bookshelf/remove")
    @Transactional
    public String removeFromBookshelf(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Story story = storyRepository.findById(storyId).orElseThrow();
        bookshelfRepository.deleteByUserIdAndStoryId(user.getId(), story.getId());

        return "redirect:/story/" + story.getSlug();
    }

    @PostMapping("/bookshelf/toggle-notify")
    @ResponseBody
    public ResponseEntity<String> toggleNotification(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Bookshelf entry = bookshelfRepository.findByUserIdAndStoryId(user.getId(), storyId)
                .orElseThrow(() -> new RuntimeException("Story not in bookshelf"));
        entry.setNotifyOnNewChapter(!entry.getNotifyOnNewChapter());
        bookshelfRepository.save(entry);
        return ResponseEntity.ok("Success");
    }
}
