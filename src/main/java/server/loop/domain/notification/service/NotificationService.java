package server.loop.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.notification.dto.res.NotificationResponseDto;
import server.loop.domain.notification.entity.Notification;
import server.loop.domain.notification.entity.repository.NotificationRepository;
import server.loop.domain.notification.event.NotificationSavedEvent;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void send(User sender, User receiver, Post post, Comment comment, String postTitle, String message) {
        if (sender.getId().equals(receiver.getId())) return;

        log.info("[SendNotification] sender={}, receiver={}, type={}", sender.getEmail(), receiver.getEmail(), message);

        Notification notification = Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .post(post)
                .comment(comment)
                .postTitle(postTitle)
                .message(message)
                .build();

        notificationRepository.save(notification);

        NotificationResponseDto dto = NotificationResponseDto.from(notification);
        eventPublisher.publishEvent(new NotificationSavedEvent(receiver.getEmail(), dto));
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getUserNotifications(User receiver, Pageable pageable) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver, pageable)
                .map(NotificationResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsSince(User receiver, Long afterId, int size) {
        if (afterId == null) {
            return List.of();
        }
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return notificationRepository
                .findByReceiverAndIdGreaterThanOrderByIdAsc(receiver, afterId, PageRequest.of(0, boundedSize))
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림 없음"));
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new SecurityException("본인의 알림만 열람 가능");
        }
        notification.markAsRead();
    }

    @Transactional
    public void deleteAllByUser(User user) {
        notificationRepository.deleteByReceiver(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByReceiver(user);
        for (Notification n : notifications) {
            n.markAsRead();
        }
    }

    @Transactional(readOnly = true)
    public int countUnreadNotifications(User user) {
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }
}
