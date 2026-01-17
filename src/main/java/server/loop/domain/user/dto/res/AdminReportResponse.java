package server.loop.domain.user.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import server.loop.domain.report.entity.Report;
import server.loop.domain.report.entity.ReportStatus;
import server.loop.domain.report.entity.ReportTargetType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminReportResponse {
    private Long id;
    private Long reporterId;
    private ReportTargetType targetType;
    private Long targetId;
    private String reasonType;
    private String reasonDetail;
    private ReportStatus status;
    private Long handledBy;
    private LocalDateTime handledAt;

    public static AdminReportResponse from(Report report) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getHandledBy() != null ? report.getHandledBy().getId() : null,
                report.getHandledAt()
        );
    }
}
