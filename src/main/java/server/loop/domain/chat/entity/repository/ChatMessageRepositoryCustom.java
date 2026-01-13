package server.loop.domain.chat.entity.repository;

import org.springframework.data.domain.Pageable;
import server.loop.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    // 채팅방 메시지 페이징 조회 (커서 기반)
    List<ChatMessage> findMessages(String roomId, Long lastMessageId, Pageable pageable);
}
