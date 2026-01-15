package server.loop.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import server.loop.global.security.CustomUserDetailsService;
import server.loop.global.security.JwtAuthenticationFilter;
import server.loop.global.security.JwtTokenProvider;
import server.loop.global.security.oauth.CustomOAuth2UserService;
import server.loop.global.security.oauth.OAuth2SuccessHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://*.vercel.app",
                "https://loop.o-r.kr",
                "https://www.loop.o-r.kr"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // 문법 최신화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 비활성화
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        // ------------------ 공개(Public) API ------------------
                        // OAuth2 경로 허용
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()

                        //광고 표기
                        .requestMatchers(HttpMethod.GET, "/api/ads").permitAll()
                        // OPTIONS 요청은 모든 경로에 대해 허용 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ws/**", "/api/ws/**").permitAll()

                        // 스웨거 문서 및 인증 관련 엔드포인트 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/signup", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll() // 토큰 재발급
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll() // 로그아웃

                        // 게시글 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()

                        // 좋아요 Top 5 게시글 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/top-liked").permitAll()

                        // 특정 게시글의 댓글 목록 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()

                        // 약관 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/terms").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms/public").permitAll()

                        // ------------------ 인증(Authenticated) API ------------------
                        // 신고(Report) 명시 추가
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/report").authenticated()

                        // 게시글 관련
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/posts/search/**").authenticated()

                        // 댓글 관련
                        .requestMatchers(HttpMethod.POST, "/api/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()

                        // 좋아요 관련
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like").authenticated()

                        // 마이페이지 관련
                        .requestMatchers("/api/mypage/**").authenticated()

                        // 사용자 정보 관련
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/profile/nickname").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/password").authenticated()

                        // 채팅 관련
                        .requestMatchers("/api/chat/**").authenticated()

                        //알림서비스
                        .requestMatchers(HttpMethod.GET, "/api/notifications").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notifications/unread-count").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/notifications").authenticated()

                        // 나머지 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 구글 정보 처리 서비스
                        )
                        .successHandler(oAuth2SuccessHandler)     // JWT 발급 핸들러
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}