package server.loop.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "회원 정보 수정 요청 (변경할 필드만 포함)")
public class UserUpdateRequestDto {

    @Schema(description = "새로운 닉네임", example = "새로운닉네임")
    private String nickname;

    @Schema(description = "새로운 비밀번호", example = "newPassword1234")
    private String password;
}