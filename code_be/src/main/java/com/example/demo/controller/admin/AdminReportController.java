package com.example.demo.controller.admin;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Report;
import com.example.demo.entity.ReportType;
import com.example.demo.entity.Story;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mod/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;

    @GetMapping
    public String listReports(Model model, @RequestParam(defaultValue = "0") int page) {
        // Lấy danh sách báo cáo chưa xử lý
        Page<Report> reports = reportRepository.findByProcessedFalseOrderByCreatedAtDesc(PageRequest.of(page, 10));
        model.addAttribute("reports", reports);
        return "mod/report-list";
    }

    @PostMapping("/handle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> handleAction(@PathVariable Long id, @RequestParam String action) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại"));

        switch (action) {
            case "delete-comment":
                if (report.getType() == ReportType.COMMENT) {
                    commentRepository.deleteByCommentId(report.getTargetId());
                }
                break;

            case "lock-story":
                if (report.getType() == ReportType.STORY) {
                    Story story = storyRepository.findById(report.getTargetId()).orElseThrow();
                    story.setStatus(Story.StoryStatus.LOCKED);
                    storyRepository.save(story);
                }
                break;

            case "ignore":
                break;
        }
        // Đánh dấu báo cáo đã check xong
        report.setProcessed(true);
        reportRepository.save(report);

        return ResponseEntity.ok("Xử lý thành công");
    }

    @GetMapping("/check-story/{storyId}")
    public String checkStory(@PathVariable Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy truyện với ID: " + storyId));

        return "redirect:/story/" + story.getSlug();
    }

    @GetMapping("/check-comment/{commentId}")
    public String checkComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));
        return "redirect:/reader/" + comment.getChapter().getId() + "#comment-" + commentId;
    }
}