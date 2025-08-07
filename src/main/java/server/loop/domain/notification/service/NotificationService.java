package server.loop.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.notification.dto.res.NotificationResponseDto;
import server.loop.domain.notification.entity.Notification;
import server.loop.domain.notification.entity.repository.NotificationRepository;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void send(User sender, User receiver, Post post, Comment comment, String message) {
        if (sender.getId().equals(receiver.getId())) return;

        Notification notification = Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .post(post)
                .comment(comment)
                .message(message)
                .build();

        notificationRepository.save(notification);

        // WebSocket 전송
        NotificationResponseDto dto = NotificationResponseDto.from(notification);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/notifications", dto);
    }

    public List<NotificationResponseDto> getUserNotifications(User receiver) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver)
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림 없음"));
        if (!notification.getReceiver().getId().equals(user.getId()))
            throw new SecurityException("본인의 알림만 열람 가능");

        notification.markAsRead();
    }
}
