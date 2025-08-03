package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.post.dto.post.res.PostLikeResponseDto;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.post.service.LikeService;

@Tag(name = "Like", description = "좋아요 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;
    private final PostRepository postRepository;

    @Operation(summary = "게시글 좋아요 토글", description = "좋아요 토글 후 최신 상태를 반환합니다.")
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<PostLikeResponseDto> toggleLike(@PathVariable Long postId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        likeService.toggleLike(postId, userDetails.getUsername());
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        boolean likedByUser = post.getLikes().stream()
                .anyMatch(like -> like.getUser().getEmail().equals(userDetails.getUsername()));

        return ResponseEntity.ok(new PostLikeResponseDto(likedByUser, post.getLikes().size()));
    }
}