package server.loop.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.auth.service.TokenService;

@Slf4j
@Tag(name = "Token", description = "토큰 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token을 사용해 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(
            @Parameter(description = "Refresh Token 값", required = true) @RequestHeader("RefreshToken") String refreshToken) {
        log.info("[Reissue] RefreshToken received");
        TokenDto tokenDto = tokenService.reissueTokens(refreshToken);
        log.info("[Reissue] Success");
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 제거하며 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh Token 값", required = true) @RequestHeader("RefreshToken") String refreshToken) {
        log.info("[Logout] RefreshToken received");
        tokenService.logout(refreshToken);
        log.info("[Logout] Success");
        return ResponseEntity.ok().build();
    }
}