package server.loop.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.report.entity.ReportStatus;
import server.loop.domain.report.entity.ReportTargetType;
import server.loop.domain.user.dto.req.AdminBanUserRequest;
import server.loop.domain.user.dto.req.AdminSuspendUserRequest;
import server.loop.domain.user.dto.res.AdminReportResponse;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.domain.user.service.AdminService;
import server.loop.global.common.PageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    @Operation(
            summary = "신고 목록 조회",
            description = """
                    관리자용 신고 목록을 조회합니다.
                    - type/status 필터링 가능
                    - Pageable로 페이징/정렬 가능
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패(토큰 없음/만료)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음(ADMIN 아님)", content = @Content)
    })
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<AdminReportResponse>> getReports(
            @Parameter(
                    name = "type",
                    description = "신고 대상 타입 (POST/COMMENT/USER)",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) ReportTargetType type,

            @Parameter(
                    name = "status",
                    description = "신고 처리 상태 (PENDING/IN_PROGRESS/RESOLVED/REJECTED)",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) ReportStatus status,

            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getReports(type, status, pageable));
    }

    @Operation(
            summary = "신고 처리 완료(승인) - RESOLVED",
            description = "신고를 처리 완료(RESOLVED) 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "신고/관리자 사용자 없음", content = @Content)
    })
    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolve(
            @Parameter(description = "신고 ID", example = "101")
            @PathVariable Long reportId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        adminService.resolveReport(reportId, admin);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "신고 반려 - REJECTED",
            description = "신고를 반려(REJECTED) 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 처리 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "신고/관리자 사용자 없음", content = @Content)
    })
    @PatchMapping("/reports/{reportId}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "신고 ID", example = "101")
            @PathVariable Long reportId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        adminService.rejectReport(reportId, admin);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 삭제(관리자)",
            description = "관리자 권한으로 게시글을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content)
    })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", example = "55")
            @PathVariable Long postId
    ) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "댓글 삭제(관리자)",
            description = "관리자 권한으로 댓글을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "댓글 없음", content = @Content)
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", example = "777")
            @PathVariable Long commentId
    ) {
        adminService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 정지(관리자)",
            description = """
                    사용자 계정을 일정 기간 정지합니다.
                    - days: 정지 일수(최소 1)
                    - reason: 정지 사유
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 처리 완료"),
            @ApiResponse(responseCode = "400", description = "검증 실패(@Valid)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspend(
            @Parameter(description = "대상 사용자 ID", example = "12")
            @PathVariable Long userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "정지 요청 바디",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AdminSuspendUserRequest.class))
            )
            @RequestBody @Valid AdminSuspendUserRequest req
    ) {
        adminService.suspendUser(userId, req.getDays(), req.getReason());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "사용자 정지 해제(관리자)",
            description = "정지된 사용자 계정을 정상(ACTIVE) 상태로 되돌립니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 해제 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @PatchMapping("/users/{userId}/unsuspend")
    public ResponseEntity<Void> unsuspend(
            @Parameter(description = "대상 사용자 ID", example = "12")
            @PathVariable Long userId
    ) {
        adminService.unsuspendUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "사용자 영구 정지(관리자)",
            description = "사용자 계정을 영구적으로 정지(BANNED)시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영구 정지 처리 완료"),
            @ApiResponse(responseCode = "400", description = "검증 실패(@Valid)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<Void> ban(
            @Parameter(description = "대상 사용자 ID", example = "12")
            @PathVariable Long userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "영구 정지 요청 바디",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AdminBanUserRequest.class))
            )
            @RequestBody @Valid AdminBanUserRequest req
    ) {
        adminService.banUser(userId, req.getReason());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "관리자 권한 부여(관리자)",
            description = "사용자에게 관리자(ADMIN) 권한을 부여합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "권한 부여 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @PatchMapping("/users/{userId}/grant-admin")
    public ResponseEntity<Void> grantAdmin(
            @Parameter(description = "대상 사용자 ID", example = "12")
            @PathVariable Long userId
    ) {
        adminService.grantAdminRole(userId);
        return ResponseEntity.ok().build();
    }
}
