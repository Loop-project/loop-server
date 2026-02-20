package server.loop.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.notification.dto.res.NotificationResponseDto;
import server.loop.domain.notification.service.NotificationService;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.common.error.ErrorCode;
import server.loop.global.common.exception.CustomException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 알림을 페이징 처리하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    @GetMapping
    public ResponseEntity<?> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 정보", example = "page=0&size=10")
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        if (userDetails == null) {
            log.warn("[GetNotifications] UserDetails is null");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("SecurityContext에서 userDetails가 비어 있음");
        }
        log.info("[GetNotifications] user={}", userDetails.getUsername());

        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Page<NotificationResponseDto> notifications = notificationService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "누락 알림 동기화", description = "특정 알림 ID 이후 생성된 알림만 조회합니다.")
    @ApiResponse(responseCode = "200", description = "누락 알림 동기화 성공")
    @GetMapping("/sync")
    public ResponseEntity<List<NotificationResponseDto>> syncNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "마지막으로 수신한 알림 ID", example = "120")
            @RequestParam Long afterId,
            @Parameter(description = "조회 크기(최대 100)", example = "50")
            @RequestParam(defaultValue = "50") int size
    ) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        return ResponseEntity.ok(notificationService.getNotificationsSince(user, afterId, size));
    }

    @Operation(summary = "단일 알림 읽음 처리", description = "알림 ID를 기반으로 해당 알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("[ReadNotification] id={}, user={}", id, userDetails.getUsername());
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 삭제", description = "로그인한 사용자의 모든 알림을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "모든 알림 삭제 성공")
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("[DeleteAllNotifications] user={}", userDetails.getUsername());
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        notificationService.deleteAllByUser(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "모든 알림 읽음 처리 성공")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("[ReadAllNotifications] user={}", userDetails.getUsername());
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "안 읽은 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "안 읽은 알림 개수 조회 성공")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadNotificationCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        int count = notificationService.countUnreadNotifications(user);
        return ResponseEntity.ok(count);
    }
}
