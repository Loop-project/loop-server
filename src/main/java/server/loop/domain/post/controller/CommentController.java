package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.comment.req.CommentCreateRequestDto;
import server.loop.domain.post.dto.comment.req.CommentUpdateRequestDto;
import server.loop.domain.post.dto.comment.res.CommentResponseDto;
import server.loop.domain.post.service.CommentService;

import java.util.List;

@Tag(name = "Comment", description = "댓글/대댓글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 생성
    @Operation(summary = "댓글/대댓글 생성", description = "게시글에 새로운 댓글 또는 대댓글을 작성합니다.")
    @PostMapping("/comments")
    public ResponseEntity<String> createComment(@RequestBody CommentCreateRequestDto requestDto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long commentId = commentService.createComment(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("댓글이 성공적으로 생성되었습니다. ID: " + commentId);
    }

    // 특정 게시글의 댓글 목록 조회
    @Operation(summary = "게시글의 댓글 목록 조회", description = "계층 구조로 된 댓글과 대댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정", description = "작성자 본인의 댓글을 수정합니다.")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId,
                                                @RequestBody CommentUpdateRequestDto requestDto,
                                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        commentService.updateComment(commentId, requestDto, userDetails.getUsername());
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "작성자 본인의 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }
}