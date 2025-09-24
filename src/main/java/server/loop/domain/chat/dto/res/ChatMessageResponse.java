package server.loop.domain.chat.dto.res;


import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageResponse {
    private Long messageId;
    private String roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private String type;
    private long createdAt;
}