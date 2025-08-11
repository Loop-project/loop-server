package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("UPDATE Post p SET p.reportCount = p.reportCount + 1 WHERE p.id = :postId")
    void incrementReportCount(@Param("postId") Long postId);

    Optional<Post> findByIdAndIsDeletedFalse(Long id);

    // 전체 목록: 삭제되지 않은 것만
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Slice<Post> findAllActivePosts(Pageable pageable);

    // 카테고리 목록: 삭제되지 않은 것만
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Slice<Post> findAllActivePostsByCategory(@Param("category") Category category, Pageable pageable);

    // 상세: 이미 isDeleted 조건 포함 (유지)
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH c.author " +
            "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActivePostWithCommentsById(@Param("id") Long id);

    // 작성자별 목록: 삭제되지 않은 것만
    @Query("SELECT p FROM Post p WHERE p.author = :author AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Slice<Post> findActivePostsByAuthor(@Param("author") User author, Pageable pageable);
}