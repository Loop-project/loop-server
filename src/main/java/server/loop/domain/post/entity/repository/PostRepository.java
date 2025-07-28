package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 일단은 기본적인 CRUD 기능만으로 충분합니다.
    // 나중에 특정 카테고리의 게시글만 조회하는 등의 기능이 필요하면 여기에 메소드를 추가할 수 있습니다.
    // 예: List<Post> findByCategory(Category category);
    @Modifying
    @Query("UPDATE Post p SET p.reportCount = p.reportCount + 1 WHERE p.id = :postId")
    void incrementReportCount(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActivePostById(@Param("id") Long id);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false")
    List<Post> findAllActivePosts();
}