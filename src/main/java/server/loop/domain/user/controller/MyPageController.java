package server.loop.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.user.dto.res.MyPagePostResponseDto;
import server.loop.domain.user.service.MyPageService;

@Slf4j
@Tag(name = "MyPage", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    // 내가 쓴 게시글 조회
    @Operation(summary = "내가 쓴 게시글 조회", description = "로그인한 사용자가 작성한 모든 게시글을 페이지네이션으로 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("[GetMyPosts] user={}", userDetails.getUsername());
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

    // 내가 댓글 단 글 조회
    @Operation(summary = "내가 댓글 단 글 조회", description = "로그인한 사용자가 댓글을 작성했던 모든 게시글을 중복 없이 페이지네이션으로 조회합니다.")
    @GetMapping("/comments/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyCommentedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("[GetMyCommentedPosts] user={}", userDetails.getUsername());
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyCommentedPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

    // 내가 좋아요 한 글 조회
    @Operation(summary = "내가 좋아요 한 글 조회", description = "로그인한 사용자가 좋아요를 누른 모든 게시글을 페이지네이션으로 조회합니다.")
    @GetMapping("/likes/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyLikedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("[GetMyLikedPosts] user={}", userDetails.getUsername());
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyLikedPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }
}