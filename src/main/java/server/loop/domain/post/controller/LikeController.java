package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.post.res.PostLikeResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.TopLikedPostResponseDto;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.post.service.LikeService;

import java.util.List;

@Slf4j
@Tag(name = "Like", description = "좋아요 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;
    private final PostRepository postRepository;

    @Operation(summary = "게시글 좋아요 토글", description = "좋아요 토글 후 최신 상태를 반환합니다.")
    @PostMapping(value = "/posts/{postId}/like", produces = "application/json")
    public ResponseEntity<PostLikeResponseDto> toggleLike(@PathVariable Long postId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[ToggleLike] postId={}, user={}", postId, userDetails.getUsername());
        PostLikeResponseDto responseDto = likeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "전날 작성된 글 중 좋아요 Top 5", description = "전날 작성된 게시글 중 좋아요가 가장 많은 게시글 5개를 반환합니다.")
    @GetMapping("/posts/top-liked")
    public ResponseEntity<List<TopLikedPostResponseDto>> getYesterdayTopLikedPosts() {
        return ResponseEntity.ok(likeService.getYesterdayTop5LikedPosts());
    }

}