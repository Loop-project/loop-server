package server.loop.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.auth.service.TokenService;

@Tag(name = "Token", description = "토큰 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token을 사용해 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(
            @Parameter(description = "Refresh Token 값", required = true) @RequestHeader("RefreshToken") String refreshToken) {
        TokenDto tokenDto = tokenService.reissueTokens(refreshToken);
        return ResponseEntity.ok(tokenDto);
    }
}