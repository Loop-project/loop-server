package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    @Query("SELECT c FROM Comment c WHERE c.post = :post") // @Where 로 대체되면 isDeleted 불필요
    List<Comment> findActiveCommentsByPost(@Param("post") Post post);

    @Query("SELECT DISTINCT c.post FROM Comment c " +
            "LEFT JOIN FETCH c.post.author " +          // 작성자 Fetch
            "WHERE c.author = :author AND c.post.isDeleted = false " +
            "ORDER BY c.post.createdAt DESC")
    Slice<Post> findActivePostsCommentedByUser(@Param("author") User author, Pageable pageable);

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

}