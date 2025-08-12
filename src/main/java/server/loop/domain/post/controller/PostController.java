package server.loop.domain.post.controller;

import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.loop.domain.post.dto.post.req.PostCreateRequestDto;
import server.loop.domain.post.dto.post.req.PostUpdateRequestDto;
import server.loop.domain.post.dto.post.res.PostDetailResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.service.PostService;
import server.loop.domain.user.entity.User;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Post", description = "게시글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성하고 이미지 파일을 업로드합니다.")
    @PostMapping(consumes = "multipart/form-data") // 1. 요청 타입을 multipart/form-data로 지정
    public ResponseEntity<String> createPost(
            @RequestPart("requestDto") PostCreateRequestDto requestDto, // 2. JSON 데이터 부분
            @RequestPart(value = "images", required = false) List<MultipartFile> images, // 3. 파일 부분
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        System.out.println("[CREATE_POST] dto=" + requestDto + ", category=" + requestDto.getCategory()
                + ", principal=" + (userDetails!=null?userDetails.getUsername():"null"));
        Long postId = postService.createPost(requestDto, images, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 생성되었습니다. ID: " + postId);
    }

    // 게시글 단건 조회
    @Operation(summary = "게시글 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal @Nullable User currentUser // 로그인 안 해도 null
    ) {
        PostDetailResponseDto response = postService.getPost(postId, currentUser);
        return ResponseEntity.ok(response);
    }


    // 게시글 수정
    @Operation(summary = "게시글 수정", description = "작성자 본인의 게시글을 수정합니다.")
    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<String> updatePost(
            @PathVariable Long postId,
            @RequestPart("requestDto") PostUpdateRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException, IOException {
        postService.updatePost(postId, requestDto, images, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다. ID: " + postId);
    }


    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "작성자 본인의 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId,
                                                          @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        postService.deletePost(postId, userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글이 성공적으로 삭제되었습니다.");
        response.put("postId", postId);
        return ResponseEntity.ok(response);
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

    @Operation(summary = "게시글 검색", description = "카테고리와 상관없이 제목/본문에서 키워드로 최신순 검색합니다. q가 비어있으면 전체 최신순 목록과 동일합니다.")
    @GetMapping("/search")
    public ResponseEntity<SliceResponseDto<PostResponseDto>> searchPosts(
            @RequestParam(required = false, defaultValue = "") String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.searchPosts(q, pageable));
    }

}