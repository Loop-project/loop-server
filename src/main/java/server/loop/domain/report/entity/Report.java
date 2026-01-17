package server.loop.domain.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import server.loop.domain.user.entity.User;
import server.loop.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String reasonType;

    @Column(columnDefinition = "TEXT")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    private LocalDateTime handledAt;

    public static Report of(User reporter, ReportTargetType targetType, Long targetId, String reasonType, String reasonDetail) {
        Report r = new Report();
        r.reporter = reporter;
        r.targetType = targetType;
        r.targetId = targetId;
        r.reasonType = reasonType;
        r.reasonDetail = reasonDetail;
        return r;
    }

    public void start(User admin) {
        this.status = ReportStatus.IN_PROGRESS;
        this.handledBy = admin;
    }

    public void resolve(User admin) {
        this.status = ReportStatus.RESOLVED;
        this.handledBy = admin;
        this.handledAt = LocalDateTime.now();
    }

    public void reject(User admin) {
        this.status = ReportStatus.REJECTED;
        this.handledBy = admin;
        this.handledAt = LocalDateTime.now();
    }
}
