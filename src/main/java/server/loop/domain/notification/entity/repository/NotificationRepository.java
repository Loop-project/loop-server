package server.loop.domain.notification.entity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.notification.entity.Notification;
import server.loop.domain.user.entity.User;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = {"sender"})
    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    @EntityGraph(attributePaths = {"sender"})
    List<Notification> findByReceiverAndIdGreaterThanOrderByIdAsc(User receiver, Long id, Pageable pageable);

    void deleteByReceiver(User receiver);
    List<Notification> findByReceiver(User receiver);

    int countByReceiverAndIsReadFalse(User receiver);
}

