package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.report.req.PostReportRequestDto;
import server.loop.domain.post.dto.report.req.UserReportRequestDto;
import server.loop.domain.post.dto.report.res.ReportResponseDto;
import server.loop.domain.post.service.ReportService;

@Slf4j
@Tag(name = "Report", description = "신고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다. 3회 누적 시 게시글은 삭제됩니다.")
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ReportResponseDto> reportPost(@PathVariable Long postId,
                                                        @RequestBody PostReportRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[ReportPost] postId={}, user={}, reason={}", postId, userDetails.getUsername(), requestDto.getReason());
        String message = reportService.reportPost(postId, userDetails.getUsername(), requestDto.getReason());
        return ResponseEntity.ok(new ReportResponseDto(message));
    }

    @Operation(summary = "사용자 신고", description = "사용자를 신고합니다. 3회 누적 시 사용자는 정지됩니다.")
    @PostMapping("/users/{userId}/report")
    public ResponseEntity<ReportResponseDto> reportUser(@PathVariable Long userId,
                                                        @RequestBody UserReportRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[ReportUser] userId={}, user={}, reason={}", userId, userDetails.getUsername(), requestDto.getReason());
        String message = reportService.reportUser(userId, userDetails.getUsername(), requestDto.getReason());
        return ResponseEntity.ok(new ReportResponseDto(message));
    }
}