package server.loop.domain.chat.entity.repository;

import org.springframework.data.domain.Pageable;
import server.loop.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    // beforeMessageId: 과거 메시지 조회(무한 스크롤), afterMessageId: 누락 메시지 동기화
    List<ChatMessage> findMessages(String roomId, Long beforeMessageId, Long afterMessageId, Pageable pageable);
}
