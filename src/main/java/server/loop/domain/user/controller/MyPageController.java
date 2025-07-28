package server.loop.domain.user.controller;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    // 내가 쓴 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

    // 내가 댓글 단 글 조회
    @GetMapping("/comments/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyCommentedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyCommentedPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

    // 내가 좋아요 한 글 조회
    @GetMapping("/likes/posts")
    public ResponseEntity<SliceResponseDto<MyPagePostResponseDto>> getMyLikedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        SliceResponseDto<MyPagePostResponseDto> result = myPageService.getMyLikedPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }
}