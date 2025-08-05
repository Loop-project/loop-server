package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
        // ⭐ 수정 사항: 서비스에서 직접 DTO를 반환하도록 변경
        // 이렇게 하면 컨트롤러가 불필요하게 DB를 다시 조회하는 것을 방지합니다.
        PostLikeResponseDto responseDto = likeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "전날 작성된 글 중 좋아요 Top 5", description = "전날 작성된 게시글 중 좋아요가 가장 많은 게시글 5개를 반환합니다.")
    @GetMapping("/posts/top-liked")
    public ResponseEntity<List<TopLikedPostResponseDto>> getYesterdayTopLikedPosts() {
        return ResponseEntity.ok(likeService.getYesterdayTop5LikedPosts());
    }

}