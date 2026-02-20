package server.loop.domain.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageSavedEvent(ChatMessageSavedEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/chat." + event.getRoomId(), event.getPayload());
        } catch (Exception e) {
            log.error("[ChatMessageDeliveryFail] roomId={}, error={}", event.getRoomId(), e.getMessage(), e);
        }
    }
}
