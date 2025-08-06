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
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // === 0. 인증이 필요 없는 경로는 바로 통과 ===
        if (path.equals("/api/users/signup") ||
                path.equals("/api/users/login") ||
                path.equals("/api/token/reissue") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/ads") ||
                (path.startsWith("/api/posts") && request.getMethod().equals("GET")) ||
                (path.startsWith("/api/terms") && request.getMethod().equals("GET"))
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

        // === 2. 토큰이 없으면 (비로그인 요청) 그냥 통과 ===
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // === 3. 토큰이 유효하면 인증 객체 생성 ===
        if (jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 4. 다음 필터로 요청 전달
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
