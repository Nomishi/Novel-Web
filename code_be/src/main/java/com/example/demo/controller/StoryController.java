package com.example.demo.controller;

import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChapterService;
import com.example.demo.service.GenreService;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StoryController {
    private final StoryService storyService;
    private final ChapterService chapterService;
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final GenreService genreService;

    @GetMapping("/stories")
    public String listStories(
            @RequestParam(required = false) String keyword,
            Model model, 
            @PageableDefault(size = 18, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) String status) {

        Specification<Story> spec = Specification.where(null);
        //Keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("author")), likePattern.toLowerCase())
            ));
        }
        //Thể loại
        if (genreIds != null && !genreIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return root.join("genres").get("id").in(genreIds);
            });
        }
        //Status
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        Page<Story> stories = storyService.getStories(spec, pageable);
        model.addAttribute("stories", stories.getContent());
        model.addAttribute("page", stories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGenreIds", genreIds);
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("status", status);
        return "story/list";
    }

    @GetMapping("/api/test-stories-count")
    @org.springframework.web.bind.annotation.ResponseBody
    public String getStoriesCount() {
        return "Total stories: " + storyService.getStories(null, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    @GetMapping("/story/{slug}")
    public String viewStory(@PathVariable String slug,
                            @RequestParam(defaultValue = "0") int page,
                            Model model,
                            Principal principal) {
        Story story = storyService.getStoryBySlug(slug);
        Page<Chapter> chapterPage = chapterService.getChaptersByStoryId(story.getId(), page);

        model.addAttribute("story", story);
        model.addAttribute("chapterPage", chapterPage);

        boolean isFollowed = false;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            isFollowed = bookshelfRepository
                    .findByUserIdAndStoryId(user.getId(), story.getId())
                    .isPresent();
        }
        model.addAttribute("isFollowed", isFollowed);

        return "story/detail";
    }
}
