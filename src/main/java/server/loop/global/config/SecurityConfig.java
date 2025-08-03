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
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    // ✅ SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/signup", "/api/users/login", "/api/token/reissue").permitAll() // 1. 인증/인가 없이 접근 가능한 경로

                        // 💡 수정된 부분: 게시글 관련 규칙을 명확하게 분리
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll() // 2. 게시글 '조회'는 누구나 가능
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()             // 3. 게시글 '작성'은 인증된 사용자만 가능

                        .requestMatchers( // 4. 그 외 인증이 필요한 경로들
                                "/api/comments/**",
                                "/api/posts/*/like",
                                "/api/posts/*/report",
                                "/api/mypage/**"
                                // 참고: 게시글 수정(PUT), 삭제(DELETE) 규칙도 필요하다면 여기에 추가해야 합니다.
                                // 예: .requestMatchers(HttpMethod.PUT, "/api/posts/*").authenticated()
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
