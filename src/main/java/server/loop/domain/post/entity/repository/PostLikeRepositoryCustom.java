package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PostLikeRepositoryCustom {
    Slice<Post> findActivePostsLikedByUser(User user, Pageable pageable);

    // 좋아요 Top 5 게시글 조회
    List<Post> findTopPostsCreatedInPeriodOrderByLikes(LocalDateTime start, LocalDateTime end);
}
