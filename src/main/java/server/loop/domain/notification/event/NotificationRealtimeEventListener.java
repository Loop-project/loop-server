package server.loop.domain.notification.event;

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
public class NotificationRealtimeEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationSavedEvent(NotificationSavedEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.getReceiverEmail(),
                    "/queue/notifications",
                    event.getPayload()
            );
        } catch (Exception e) {
            log.error(
                    "[NotificationDeliveryFail] receiver={}, error={}",
                    event.getReceiverEmail(),
                    e.getMessage(),
                    e
            );
        }
    }
}
