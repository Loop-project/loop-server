package server.loop.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "닉네임 수정")
public class UpdateNicknameRequest {

    @Schema(description = "닉네임 수정")
    private String nickname;
}