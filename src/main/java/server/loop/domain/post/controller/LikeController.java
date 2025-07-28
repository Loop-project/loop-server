package server.loop.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.post.service.LikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        String message = likeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(message);
    }
}