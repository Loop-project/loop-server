package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    // 댓글/대댓글 생성
    public Long createComment(CommentCreateRequestDto requestDto, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Comment parentComment = null;
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

        // ✅ 알림 로직 시작
        User postAuthor = post.getAuthor();
        String postTitle = post.getTitle(); // 게시글 제목

        if (parentComment != null) {
            User parentAuthor = parentComment.getAuthor();
            if (!parentAuthor.getId().equals(author.getId())) {
                notificationService.send(author, parentAuthor, post, comment, postTitle, "Your comment has a new reply.");
            }
            if (!postAuthor.getId().equals(parentAuthor.getId()) && !postAuthor.getId().equals(author.getId())) {
                notificationService.send(author, postAuthor, post, comment, postTitle, "Your post has a new nested comment.");
            }
        } else if (!postAuthor.getId().equals(author.getId())) {
            notificationService.send(author, postAuthor, post, comment, postTitle, "Your post has a new comment.");
        }
        // ✅ 알림 로직 끝

        return comment.getId();
    }


    // 특정 게시글의 댓글 목록 조회 (계층 구조로 변환)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return commentRepository.findAllByPost(post).stream()
                .filter(comment -> comment.getParent() == null)
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
    //댓글 수정
    public Long updateComment(Long commentId, CommentUpdateRequestDto requestDto, String email) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }
        comment.update(requestDto.getContent());
        return comment.getPost().getId();
    }


    // 댓글 삭제
    public Long deleteComment(Long commentId, String email) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }
        Long postId = comment.getPost().getId();
        comment.softDelete();
        return postId;
    }

}