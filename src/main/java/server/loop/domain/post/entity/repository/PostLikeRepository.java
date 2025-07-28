package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);
}