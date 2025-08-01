package server.loop.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.auth.entity.RefreshToken;
import server.loop.domain.auth.entity.repo.RefreshTokenRepository;
import server.loop.global.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenDto reissueTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
        }

        RefreshToken foundRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Refresh Token 입니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(foundRefreshToken.getUser().getEmail());

        String newRefreshToken = jwtTokenProvider.createRefreshToken(foundRefreshToken.getUser().getEmail());
        foundRefreshToken.updateToken(newRefreshToken);

        return new TokenDto(newAccessToken, newRefreshToken);
    }
}