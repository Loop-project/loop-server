package server.loop.domain.user.dto.res;

import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "사용자 응답 DTO")
public class UserResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private final Long id;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private final String email;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private final String nickname;

    public UserResponseDto(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
