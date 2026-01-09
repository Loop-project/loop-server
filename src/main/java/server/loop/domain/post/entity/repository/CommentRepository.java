package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.post.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
}
