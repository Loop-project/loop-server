package server.loop.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.post.dto.report.req.PostReportRequestDto;
import server.loop.domain.post.service.ReportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<String> reportPost(@PathVariable Long postId,
                                             @RequestBody PostReportRequestDto requestDto, // DTO로 받도록 변경
                                             @AuthenticationPrincipal UserDetails userDetails) {
        // 서비스에 DTO의 reason을 함께 전달
        String message = reportService.reportPost(postId, userDetails.getUsername(), requestDto.getReason());
        return ResponseEntity.ok(message);
    }
}