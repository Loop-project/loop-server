package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

public interface CommentRepositoryCustom {
    // 특정 게시글의 댓글 조회 (삭제된 것 제외 + 작성자 페치조인)
    List<Comment> findAllByPost(Post post);
    
    // 내가 댓글 단 게시글 목록 조회
    Slice<Post> findActivePostsCommentedByUser(User author, Pageable pageable);
}