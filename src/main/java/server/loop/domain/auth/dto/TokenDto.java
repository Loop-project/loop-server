package server.loop.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 성공 시 발급되는 토큰 정보")
public class TokenDto {

    @Schema(description = "API 접근 시 필요한 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QG...")
    private String accessToken;

    @Schema(description = "Access Token 재발급 시 필요한 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QG...")
    private String refreshToken;
}