package server.loop.domain.chat.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.chat.entity.ChatMessage;

import java.util.List;
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoom_IdOrderByIdDesc(String roomId, Pageable pageable);
    List<ChatMessage> findByRoom_IdAndIdLessThanOrderByIdDesc(String roomId, Long beforeId, Pageable pageable);
    long countByRoom_Id(String roomId);
}