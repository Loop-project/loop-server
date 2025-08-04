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

    @Operation(summary = "댓글/대댓글 생성", description = "작성 후 최신 댓글 리스트를 반환합니다.")
    @PostMapping(value = "/comments", produces = "application/json") // 수정
    public ResponseEntity<List<CommentResponseDto>> createComment(@RequestBody CommentCreateRequestDto requestDto,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        commentService.createComment(requestDto, userDetails.getUsername());
        List<CommentResponseDto> updatedComments = commentService.getCommentsByPost(requestDto.getPostId());
        return ResponseEntity.ok(updatedComments);
    }

    // 특정 게시글의 댓글 목록 조회
    @Operation(summary = "게시글의 댓글 목록 조회", description = "계층 구조로 된 댓글과 대댓글 목록을 조회합니다.")
    @GetMapping(value = "/posts/{postId}/comments", produces = "application/json") // 수정
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 수정", description = "작성자 본인의 댓글을 수정 후 최신 댓글 리스트를 반환합니다.")
    @PutMapping(value = "/comments/{commentId}", produces = "application/json") // 수정
    public ResponseEntity<List<CommentResponseDto>> updateComment(@PathVariable Long commentId,
                                                                  @RequestBody CommentUpdateRequestDto requestDto,
                                                                  @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Long postId = commentService.updateComment(commentId, requestDto, userDetails.getUsername());
        List<CommentResponseDto> updatedComments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(updatedComments);
    }

    @Operation(summary = "댓글 삭제", description = "작성자 본인의 댓글을 삭제 후 최신 댓글 리스트를 반환합니다.")
    @DeleteMapping(value = "/comments/{commentId}", produces = "application/json") // 수정
    public ResponseEntity<List<CommentResponseDto>> deleteComment(@PathVariable Long commentId,
                                                                  @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Long postId = commentService.deleteComment(commentId, userDetails.getUsername());
        List<CommentResponseDto> updatedComments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(updatedComments);
    }
}
