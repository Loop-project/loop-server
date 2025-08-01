package server.loop.domain.post.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostReport;
import server.loop.domain.post.entity.repository.PostReportRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private static final int REPORT_THRESHOLD = 3;

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EntityManager em;

    public String reportPost(Long postId, String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다."));

        if (postReportRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        postReportRepository.save(new PostReport(user, post, reason));
        postRepository.incrementReportCount(postId);
        em.clear();

        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("게시글을 다시 조회하는 중 오류 발생"));

        if (updatedPost.getReportCount() >= REPORT_THRESHOLD) {
            updatedPost.softDelete();
            return "신고가 3회 누적되어 게시글이 블라인드 처리되었습니다.";
        }

        return "신고가 정상적으로 접수되었습니다.";
    }
}
