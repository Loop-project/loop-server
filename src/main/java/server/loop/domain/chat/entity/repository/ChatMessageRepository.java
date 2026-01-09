package server.loop.domain.chat.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    long countByRoom_Id(String roomId);
}
