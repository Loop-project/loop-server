package server.loop.domain.report.entity;

public enum ReportStatus {
    PENDING,        // 처리 대기 중
    IN_PROGRESS,    // 처리 진행 중
    RESOLVED,       // 처리 완료 (승인)
    REJECTED        // 처리 거부 (반려)
}
