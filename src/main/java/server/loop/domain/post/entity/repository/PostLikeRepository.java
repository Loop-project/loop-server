package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);

    @Query("SELECT pl.post FROM PostLike pl " +
            "LEFT JOIN FETCH pl.post.author " +   // 작성자 Fetch
            "WHERE pl.user = :user AND pl.post.isDeleted = false " +
            "ORDER BY pl.post.createdAt DESC")
    Slice<Post> findActivePostsLikedByUser(@Param("user") User user, Pageable pageable);
}