package server.loop.domain.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import server.loop.domain.notification.dto.res.NotificationResponseDto;

@Getter
@RequiredArgsConstructor
public class NotificationSavedEvent {

    private final String receiverEmail;
    private final NotificationResponseDto payload;
}
