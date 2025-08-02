package server.loop.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // 프론트 개발 주소
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Preflight 허용
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization") // 프론트에서 토큰을 읽을 수 있도록
                        .allowCredentials(true);
            }
        };
    }
}