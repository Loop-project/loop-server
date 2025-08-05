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
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://3.35.135.211",
                "https://loop.o-r.kr",
                "https://www.loop.o-r.kr",
                "http://52.78.82.75"
        ));
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
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/users/signup", "/api/users/login", "/api/token/reissue").permitAll()

                        // 1. 게시글 관련 규칙
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll() // 게시글 조회는 누구나
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()            // 게시글 작성은 인증된 사용자만
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()           // 게시글 수정은 인증된 사용자만
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()        // 게시글 삭제는 인증된 사용자만

                        // 2. 댓글 관련 규칙
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()     // 댓글 조회는 누구나
                        .requestMatchers(HttpMethod.POST, "/api/comments").authenticated()          // 댓글 작성은 인증된 사용자만
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()        // 댓글 수정은 인증된 사용자만
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()     // 댓글 삭제는 인증된 사용자만

                        // 3. 좋아요, 마이페이지 등 기타 규칙
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like").authenticated()      // 좋아요는 인증된 사용자만
                        .requestMatchers("/api/mypage/**").authenticated()                          // 마이페이지는 인증된 사용자만

                        // 4.  광고 (누구나 접근 가능)
                        .requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ads").permitAll()


                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}