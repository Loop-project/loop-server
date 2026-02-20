package server.loop.domain.chat.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import server.loop.domain.chat.dto.res.ChatMessageResponse;

@Getter
@RequiredArgsConstructor
public class ChatMessageSavedEvent {

    private final String roomId;
    private final ChatMessageResponse payload;
}
