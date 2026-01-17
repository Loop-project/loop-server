package server.loop.domain.post.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.report.entity.Report;
import server.loop.domain.report.entity.ReportRepository;
import server.loop.domain.report.entity.ReportTargetType;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private static final int USER_REPORT_THRESHOLD = 3;

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public String reportPost(Long postId, String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다."));

        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(user, ReportTargetType.POST, postId)) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        Report report = Report.of(user, ReportTargetType.POST, postId, reason, "");
        reportRepository.save(report);

        long reportCount = reportRepository.countByTargetTypeAndTargetId(ReportTargetType.POST, postId);

        if (reportCount >= 3) {
            post.softDelete();
            return "신고가 3회 누적되어 게시글이 블라인드 처리되었습니다.";
        }

        return "신고가 정상적으로 접수되었습니다.";
    }

    public String reportUser(Long targetUserId, String reporterEmail, String reason) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고자입니다."));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (reporter.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("자기 자신을 신고할 수 없습니다.");
        }

        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, ReportTargetType.USER, targetUserId)) {
            throw new IllegalStateException("이미 신고한 사용자입니다.");
        }

        Report report = Report.of(reporter, ReportTargetType.USER, targetUserId, reason, "");
        reportRepository.save(report);

        long reportCount = reportRepository.countByTargetTypeAndTargetId(ReportTargetType.USER, targetUserId);

        if (reportCount >= USER_REPORT_THRESHOLD) {
            targetUser.suspend(7, "다수 신고로 인한 자동 정지"); // 7일 정지
            userRepository.save(targetUser); // 변경사항 저장
            return "신고가 " + USER_REPORT_THRESHOLD + "회 누적되어 사용자가 7일간 정지 처리되었습니다.";
        }

        return "사용자 신고가 정상적으로 접수되었습니다.";
    }
}
