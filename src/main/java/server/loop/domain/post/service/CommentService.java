package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.dto.comment.req.CommentCreateRequestDto;
import server.loop.domain.post.dto.comment.req.CommentUpdateRequestDto;
import server.loop.domain.post.dto.comment.res.CommentResponseDto;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.CommentRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 댓글/대댓글 생성
    public Long createComment(CommentCreateRequestDto requestDto, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Comment parentComment = null;
        // 부모 댓글 ID가 있으면 찾아서 설정 (대댓글)
        if (requestDto.getParentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
        }

        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .parent(parentComment)
                .content(requestDto.getContent())
                .build();

        commentRepository.save(comment);
        return comment.getId();
    }

    // 특정 게시글의 댓글 목록 조회 (계층 구조로 변환)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return commentRepository.findByPost(post).stream()
                .filter(comment -> comment.getParent() == null) // 최상위 댓글(부모 없는 댓글)만 필터링
                .map(CommentResponseDto::new) // DTO로 변환
                .collect(Collectors.toList());
    }
    public void updateComment(Long commentId, CommentUpdateRequestDto requestDto, String email) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 작성자 본인인지 확인
        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(requestDto.getContent());
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String email) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 작성자 본인인지 확인
        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}