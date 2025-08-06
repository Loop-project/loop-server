package server.loop.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import server.loop.global.security.CustomUserDetailsService;
import server.loop.global.security.JwtAuthenticationFilter;
import server.loop.global.security.JwtTokenProvider;

import java.util.List;

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
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ------------------ 공개(Public) API ------------------
                        //광고 표기
                        .requestMatchers(HttpMethod.GET, "/api/ads").permitAll()
                        // OPTIONS 요청은 모든 경로에 대해 허용 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 스웨거 문서 및 인증 관련 엔드포인트 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/signup", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/token/reissue").permitAll() // 토큰 재발급

                        // 게시글 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()

                        // 좋아요 Top 5 게시글 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/top-liked").permitAll()

                        // 특정 게시글의 댓글 목록 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()

                        // 약관 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/terms").permitAll()

                        // ------------------ 인증(Authenticated) API ------------------
                        // 게시글 관련
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

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

                        // 나머지 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
