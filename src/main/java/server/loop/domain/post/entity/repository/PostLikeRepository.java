package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long>, PostLikeRepositoryCustom {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    //특정 게시글의 좋아요 개수 조회
    long countByPost(Post post);

    // 특정 사용자의 모든 좋아요 삭제
    void deleteByUser(User user);
}
