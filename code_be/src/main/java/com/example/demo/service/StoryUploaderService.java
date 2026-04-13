package com.example.demo.service;

import com.example.demo.entity.Genre;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor

public class StoryUploaderService {
    private final StoryRepository storyRepository;
    private final StoryService storyService;
    private final ChapterRepository chapterRepository;
    private final GenreRepository genreRepository;

    public List<Story> getStoriesByUploader(User user) {
        return storyRepository.findByUploader(user);
    }

    public boolean isOwner(Long storyId, User user) {
        Story story = storyRepository.findById(storyId).orElse(null);
        return story != null && story.getUploader() != null
               && story.getUploader().getId().equals(user.getId());
    }
    
    @Transactional
    public void deleteByOwner(Long storyId, User user) {
        if (isOwner(storyId, user)) {
            storyService.deleteStory(storyId); 
        } else {
            throw new RuntimeException("Bạn không có quyền xóa truyện này!");
        }
    }

    @Transactional
    public Story uploadNewStory(Story story, List<Long> genreIds, User uploader) {
        story.setUploader(uploader);
        story.setViews(0L);
        story.setNominations(0L);

        if (genreIds != null && !genreIds.isEmpty()) {
            List<Genre> selectedGenres = genreRepository.findAllById(genreIds);
            story.setGenres(new java.util.HashSet<>(selectedGenres));
        }
        if (story.getSlug() == null || story.getSlug().isEmpty()) {
            story.setSlug(story.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        return storyRepository.save(story);
    }
    
    @Transactional
    public Chapter addChapterByOwner(Long storyId, Chapter chapter, User user) {
        if (!isOwner(storyId, user)) {
            throw new RuntimeException("Bạn không có quyền thêm chương cho truyện này!");
        }
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));
        chapter.setStory(story);
    
        if (chapter.getChapterNumber() == null) {
            List<Chapter> existingChapters = chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
            double nextNumber = existingChapters.isEmpty() ? 1.0 : 
                                existingChapters.get(existingChapters.size() - 1).getChapterNumber() + 1.0;
            chapter.setChapterNumber(nextNumber);
        }
        if (chapter.getType() == null) {
            chapter.setType(Chapter.ChapterType.TEXT);
        }

        return chapterRepository.save(chapter);
    }

    @Transactional
    public Story updateStoryByOwner(Long storyId, Story updatedData, List<Long> genreIds, User user) {
        if (!isOwner(storyId, user)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa truyện này!");
        }

        Story existingStory = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));
        existingStory.setTitle(updatedData.getTitle());
        existingStory.setAuthor(updatedData.getAuthor());
        existingStory.setDescription(updatedData.getDescription());
        existingStory.setStatus(updatedData.getStatus());
        existingStory.getGenres().clear();
        existingStory.setCoverImage(updatedData.getCoverImage());

        if (genreIds != null && !genreIds.isEmpty()) {
            List<Genre> selectedGenres = genreRepository.findAllById(genreIds);
            existingStory.setGenres(new java.util.HashSet<>(selectedGenres));
        }

        return storyRepository.save(existingStory);
    }

    @Transactional
    public void updateChapterByOwner(Long chapterId, Chapter newData, User user) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương"));

        if (!chapter.getStory().getUploader().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa chương của truyện này");
        }

        chapter.setChapterNumber(newData.getChapterNumber());
        chapter.setTitle(newData.getTitle());
        chapter.setContent(newData.getContent());
    }
}