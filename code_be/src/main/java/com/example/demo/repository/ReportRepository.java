package com.example.demo.repository;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    //Báo cáo chưa xử lý, sắp xếp mới nhất lên đầu
    Page<Report> findByProcessedFalseOrderByCreatedAtDesc(Pageable pageable);

    //User đã báo cáo mục này chưa
    boolean existsByReporterIdAndTypeAndTargetId(Long reporterId, ReportType type, Long targetId);

    //Lịch sử báo cáo của user
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    long countByReporterIdAndTypeAndTargetId(Long reporterId, ReportType type, Long targetId);
}