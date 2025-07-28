package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.isDeleted = false")
    List<Comment> findActiveCommentsByPost(@Param("post") Post post);
}