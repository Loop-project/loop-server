package server.loop.global.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import server.loop.domain.auth.entity.RefreshToken;
import server.loop.domain.auth.entity.repo.RefreshTokenRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.security.JwtTokenProvider;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        // 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. 토큰 생성 (인자 개수는 기존 JwtTokenProvider에 맞춰 수정하세요)
        // 예: createAccessToken(email, role) 형태일 수도 있음
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 2. Refresh Token DB 저장
        RefreshToken tokenEntity = refreshTokenRepository.findByUser(user)
                .map(entity -> entity.updateToken(refreshToken))
                .orElse(new RefreshToken(user, refreshToken));
        refreshTokenRepository.save(tokenEntity);

        // 3. 프론트엔드로 리다이렉트
        //로컬용
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

//        String targetUrl = UriComponentsBuilder.fromUriString("https://loop-front-eta.vercel.app/oauth/callback")
//                .queryParam("accessToken", accessToken)
//                .queryParam("refreshToken", refreshToken)
//                .build().toUriString();

        log.info("소셜 로그인 성공! 리다이렉트: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}