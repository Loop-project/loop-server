package server.loop.domain.chat.dto.res;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomResponse {
    private String roomId;
    private String title;
    private String visibility;
    private Long ownerId;
    private Long memberCount;
    private Long createdAt;
    private boolean joined;
    private Long postId;
    private List<ChatRoomMemberResponse> members;
}