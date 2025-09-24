package server.loop.domain.chat.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageSendRequest {
    @NotBlank
    private String roomId;
    @NotBlank
    private String content;
    @Builder.Default
    private String type = "TEXT";
}