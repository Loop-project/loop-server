package server.loop.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import server.loop.global.security.CustomUserDetailsService;
import server.loop.global.security.JwtAuthenticationFilter;
import server.loop.global.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight (CORS) 요청은 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger 문서
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/swagger-resources"
                        ).permitAll()

                        // 회원가입/로그인/토큰 재발급
                        .requestMatchers(
                                "/api/users/signup", "/api/users/signup/**",
                                "/api/users/login", "/api/users/login/**",
                                "/api/token/reissue"
                        ).permitAll()

                        // 게시글 목록 조회 & 단건 조회 (비로그인 허용)
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

                        // 마이페이지 API → 로그인 필요
                        .requestMatchers("/api/mypage/**").authenticated()

                        // 나머지 (댓글, 좋아요, 신고, 게시글 생성·수정·삭제 등)는 로그인 필요
                        .requestMatchers(
                                "/api/comments/**",
                                "/api/posts/**",
                                "/api/posts/*/like",
                                "/api/posts/*/report"
                        ).authenticated()

                        // 기타 모든 요청도 인증
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
