package server.loop.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.report.req.PostReportRequestDto;
import server.loop.domain.post.dto.report.res.ReportResponseDto;
import server.loop.domain.post.service.ReportService;

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
        String message = reportService.reportPost(postId, userDetails.getUsername(), requestDto.getReason());
        return ResponseEntity.ok(new ReportResponseDto(message));
    }
}