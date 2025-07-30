package server.loop.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청")
public class UserLoginDto {

    @Schema(description = "이메일", example = "test@loop.com")
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    private String password;
}