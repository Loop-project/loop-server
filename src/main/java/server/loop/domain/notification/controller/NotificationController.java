package server.loop.domain.notification.controller;


import lombok.RequiredArgsConstructor;
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


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("SecurityContext에서 userDetails가 비어 있음");
        }

        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        Page<NotificationResponseDto> notifications = notificationService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        notificationService.deleteAllByUser(user);
        return ResponseEntity.ok().build();
    }
}