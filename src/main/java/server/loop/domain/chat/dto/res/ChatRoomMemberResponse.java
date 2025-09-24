package server.loop.domain.chat.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomMemberResponse {
    private Long userId;
    private String nickname;
}
