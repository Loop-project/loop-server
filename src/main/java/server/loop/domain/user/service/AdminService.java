package server.loop.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.CommentRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.report.entity.Report;
import server.loop.domain.report.entity.ReportRepository;
import server.loop.domain.report.entity.ReportStatus;
import server.loop.domain.report.entity.ReportTargetType;
import server.loop.domain.user.dto.res.AdminReportResponse;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.common.PageResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 신고 목록을 필터링 조회
    public PageResponse<AdminReportResponse> getReports(ReportTargetType type, ReportStatus status, Pageable pageable) {
        Page<Report> page;
        if (type != null && status != null) page = reportRepository.findByTargetTypeAndStatus(type, status, pageable);
        else if (type != null) page = reportRepository.findByTargetType(type, pageable);
        else if (status != null) page = reportRepository.findByStatus(status, pageable);
        else page = reportRepository.findAll(pageable);

        return PageResponse.from(page.map(AdminReportResponse::from));
    }

    // 특정 신고를 '처리 완료(RESOLVED)

    public void resolveReport(Long reportId, User admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        report.resolve(admin);
    }

    //특정 신고를 '반려(REJECTED)' 상태로 변경
    public void rejectReport(Long reportId, User admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        report.reject(admin);
    }

    //관리자 권한으로 특정 게시글을 삭제
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        post.softDelete();
    }

    //관리자 권한으로 특정 댓글을 논리적으로 삭제
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        comment.softDelete();
    }

    // 특정 사용자를 지정된 기간 동안 정지
    public void suspendUser(Long userId, int days, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.suspend(days, reason);
    }

    //특정 사용자의 정지를 해제합니다.
    public void unsuspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.unsuspend();
    }

    // 특정 사용자를 영구적으로 정지
    public void banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.ban(reason);
    }

    public void grantAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.grantAdmin();
    }
}
