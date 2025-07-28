package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostReport;
import server.loop.domain.user.entity.User;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByUserAndPost(User user, Post post);
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    List<Post> findAllActivePostsByCategory(@Param("category") Category category);
}