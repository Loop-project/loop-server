package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);

    @Query("SELECT pl.post FROM PostLike pl " +
            "LEFT JOIN FETCH pl.post.author " +   // 작성자 Fetch
            "WHERE pl.user = :user AND pl.post.isDeleted = false " +
            "ORDER BY pl.post.createdAt DESC")
    Slice<Post> findActivePostsLikedByUser(@Param("user") User user, Pageable pageable);

    //좋아요 top 5
    @Query(value = """
    SELECT p.* 
    FROM post p
    LEFT JOIN post_like l ON p.id = l.post_id
    WHERE p.created_at BETWEEN :start AND :end
    GROUP BY p.id
    ORDER BY COUNT(l.id) DESC, p.created_at DESC
    LIMIT 5
""", nativeQuery = true)
    List<Post> findTopPostsCreatedInPeriodOrderByLikesNative(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}