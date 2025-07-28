package server.loop.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.comment.req.CommentCreateRequestDto;
import server.loop.domain.post.dto.comment.res.CommentResponseDto;
import server.loop.domain.post.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 생성
    @PostMapping("/comments")
    public ResponseEntity<String> createComment(@RequestBody CommentCreateRequestDto requestDto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long commentId = commentService.createComment(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("댓글이 성공적으로 생성되었습니다. ID: " + commentId);
    }

    // 특정 게시글의 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }
}