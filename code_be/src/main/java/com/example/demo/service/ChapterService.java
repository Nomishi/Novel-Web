package com.example.demo.service;

import com.example.demo.entity.Chapter;
import com.example.demo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final NotificationService notificationService;

    public List<Chapter> getChaptersByStoryId(Long storyId) {
        return chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
    }

    public Long getNextChapterId(Long storyId, Double currentNum) {
        return chapterRepository.findFirstByStoryIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(storyId, currentNum)
                .map(Chapter::getId).orElse(null);
    }

    public Long getPrevChapterId(Long storyId, Double currentNum) {
        return chapterRepository.findFirstByStoryIdAndChapterNumberLessThanOrderByChapterNumberDesc(storyId, currentNum)
                .map(Chapter::getId).orElse(null);
    }

    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
    }

    public Page<Chapter> getChaptersByStoryId(Long storyId, int page) {
        return chapterRepository.findByStoryId(
                storyId,
                PageRequest.of(page, 40, Sort.by("chapterNumber").ascending()));
    }

    @Transactional
    public Chapter saveChapter(Chapter chapter) {
        Chapter saved = chapterRepository.save(chapter);
        notificationService.notifyNewChapter(saved);
        return saved;
    }
    @Transactional
    public void deleteChapter(Long id) {
        chapterRepository.deleteById(id);
    }
}
