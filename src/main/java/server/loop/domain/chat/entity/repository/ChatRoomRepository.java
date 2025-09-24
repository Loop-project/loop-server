package server.loop.domain.chat.entity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.chat.entity.ChatRoom;
import server.loop.domain.user.entity.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Page<ChatRoom> findByVisibility(String visibility, Pageable pageable);
    Page<ChatRoom> findByOwner(User owner, Pageable pageable);
}