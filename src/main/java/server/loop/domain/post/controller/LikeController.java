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
    // LikeController.java

    @PostMapping(value = "/posts/{postId}/like", produces = "application/json")
    public ResponseEntity<PostLikeResponseDto> toggleLike(@PathVariable Long postId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {

        // ======================= 테스트용 코드 =======================
        // 실제 로직을 무시하고, 무조건 좋아요 개수를 999로 반환합니다.
        // 서버 로그에도 메시지를 출력합니다.
        System.out.println("!!!!!!!!!! 좋아요 API 최종 테스트 코드 실행됨 !!!!!!!!!!");
        return ResponseEntity.ok(new PostLikeResponseDto(true, 999));
        // ===========================================================

    /* // 기존 코드는 잠시 주석 처리
    likeService.toggleLike(postId, userDetails.getUsername());
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    boolean likedByUser = post.getLikes().stream()
            .anyMatch(like -> like.getUser().getEmail().equals(userDetails.getUsername()));

    return ResponseEntity.ok(new PostLikeResponseDto(likedByUser, post.getLikes().size()));
    */
    }
}