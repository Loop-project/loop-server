package server.loop.domain.notification.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.notification.entity.Notification;
import server.loop.domain.user.entity.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
}
