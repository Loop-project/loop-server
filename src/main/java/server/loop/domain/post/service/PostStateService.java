package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostStateService {

    private static final int REPORT_THRESHOLD = 3;
    private final PostRepository postRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String checkAndBlindPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (post.getReportCount() >= REPORT_THRESHOLD) {
            post.softDelete();
            return "신고가 3회 누적되어 게시글이 블라인드 처리되었습니다.";
        }

        return "신고가 정상적으로 접수되었습니다.";
    }
}
