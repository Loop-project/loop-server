package server.loop.domain.notification.entity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.notification.entity.Notification;
import server.loop.domain.user.entity.User;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = {"sender"})
    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);
}

