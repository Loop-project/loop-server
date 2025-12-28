package server.loop.domain.chat.dto.res;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomResponse {
    private String roomId;
    private String title;
    private String visibility;
    private Long ownerId;
    private long memberCount;
    private long createdAt;
    private boolean joined; // 현재 사용자 관점
    private List<ChatRoomMemberResponse> members;
}