package server.loop.domain.report.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.user.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    Page<Report> findByTargetType(ReportTargetType targetType, Pageable pageable);
    Page<Report> findByTargetTypeAndStatus(ReportTargetType targetType, ReportStatus status, Pageable pageable);

    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}
