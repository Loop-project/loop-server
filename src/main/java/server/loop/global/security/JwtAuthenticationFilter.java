package server.loop.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // CustomUserDetailsService 주입

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // === 1. 광고 업로드/조회 API는 인증 없이 통과 ===
        if (path.startsWith("/api/ads")) {  // /api/ads 또는 /api/ads/… 전부 허용
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 사용자 정보(email) 가져오기
            String email = jwtTokenProvider.getEmail(token);
            // 4. UserDetailsService를 통해 UserDetails 객체 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            // 5. Authentication 객체 생성 및 SecurityContext에 저장
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // 요청 헤더에서 'Bearer ' 토큰을 추출하는 메소드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}