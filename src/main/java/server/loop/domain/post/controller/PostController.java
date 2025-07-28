package server.loop.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.req.PostCreateRequestDto;
import server.loop.domain.post.dto.req.PostUpdateRequestDto;
import server.loop.domain.post.dto.res.PostResponseDto;
import server.loop.domain.post.service.PostService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 1. 게시글 생성
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostCreateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails.getUsername() 은 CustomUserDetailsService 에서 우리가 넣어준 email 입니다.
        Long postId = postService.createPost(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 생성되었습니다. ID: " + postId);
    }

    // 2. 게시글 단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        PostResponseDto postResponseDto = postService.getPost(postId);
        return ResponseEntity.ok(postResponseDto);
    }

    // 3. 게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // 4. 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId,
                                             @RequestBody PostUpdateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        postService.updatePost(postId, requestDto, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다. ID: "+postId);
    }

    // 5. 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId,
                                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다. ID: "+postId);
    }
}