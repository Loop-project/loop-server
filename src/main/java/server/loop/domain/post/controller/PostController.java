package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.post.req.PostCreateRequestDto;
import server.loop.domain.post.dto.post.req.PostUpdateRequestDto;
import server.loop.domain.post.dto.post.res.PostDetailResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.service.PostService;
import server.loop.domain.user.entity.User;

import java.nio.file.AccessDeniedException;

@Tag(name = "Post", description = "게시글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostCreateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails.getUsername() 은 CustomUserDetailsService 에서 우리가 넣어준 email 입니다.
        Long postId = postService.createPost(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 생성되었습니다. ID: " + postId);
    }

    // 게시글 단건 조회
    @Operation(summary = "게시글 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        PostDetailResponseDto response = postService.getPost(postId, currentUser);
        return ResponseEntity.ok(response);
    }


    // 게시글 수정
    @Operation(summary = "게시글 수정", description = "작성자 본인의 게시글을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId,
                                             @RequestBody PostUpdateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        postService.updatePost(postId, requestDto, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다. ID: "+postId);
    }

    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "작성자 본인의 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId,
                                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다. ID: "+postId);
    }

    // 카테고리 별 게시글 조회
    @Operation(summary = "게시글 목록 조회 (Page)", description = "카테고리별 또는 전체 게시글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<SliceResponseDto<PostResponseDto>> getPostsSlice(
            @RequestParam(required = false) Category category,
            // 기본 20개씩, 최신순으로 정렬
            @PageableDefault(size = 20) Pageable pageable) {

        SliceResponseDto<PostResponseDto> result = postService.getPostsSlice(category, pageable);
        return ResponseEntity.ok(result);
    }
}