package server.loop.domain.notification.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.notification.dto.res.NotificationResponseDto;
import server.loop.domain.notification.service.NotificationService;
import server.loop.domain.user.entity.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println(">>> userDetails: " + userDetails); // 로그 확인
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "SecurityContext에서 userDetails가 비어 있음"));
        }

        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(notificationService.getUserNotifications(user));
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
}
