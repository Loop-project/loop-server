package server.loop.domain.notification.dto.res;

import lombok.*;
import server.loop.domain.notification.entity.Notification;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDto {

    private Long id;
    private String senderUsername;
    private String message;
    private boolean isRead;
    private String postTitle;
    private Long postId;
    private Long commentId;
    private LocalDateTime createdAt;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .senderUsername(notification.getSender().getNickname())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .postId(notification.getPost() != null ? notification.getPost().getId() : null)
                .postTitle(notification.getPostTitle())
                .commentId(notification.getComment() != null ? notification.getComment().getId() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
