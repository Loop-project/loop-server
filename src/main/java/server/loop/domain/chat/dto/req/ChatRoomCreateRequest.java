package server.loop.domain.chat.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomCreateRequest {
    @NotBlank
    private String title;
    @Builder.Default
    private String visibility = "PUBLIC"; // PUBLIC(누구나 참여) | PRIVATE(추후 확장용)
}