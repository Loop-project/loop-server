package server.loop.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 1. 토큰이 없으면 통과 (익명 사용자 허용됨)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 토큰 유효성 검증 후 SecurityContext 설정
        if (jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmail(token);
            log.info("유효한 토큰. 이메일: {}", email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Jwt 필터 통과");
            log.debug("토큰 유효 여부: {}", jwtTokenProvider.validateToken(token));
            log.debug("이메일: {}", jwtTokenProvider.getEmail(token));
            log.debug("SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());

        } else {
            log.warn("유효하지 않은 토큰");
        }

        // 3. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }


    // 요청 헤더에서 'Bearer ' 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
