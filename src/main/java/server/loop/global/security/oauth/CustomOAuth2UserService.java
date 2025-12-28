package server.loop.global.security.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 이메일 추출
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("구글에서 이메일 정보를 받아오지 못했습니다.");
        }

        // 3. DB 저장 (가입되어 있으면 조회, 없으면 새로 생성)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name));

        // 4. 리턴 (Role은 필요에 따라 설정)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub" // 구글의 PK 필드명
        );
    }

    private User createUser(String email, String name) {
        // 닉네임 중복 방지 (예: 홍길동_a1b2c)
        String uniqueNickname = name + "_" + UUID.randomUUID().toString().substring(0, 5);

        log.info("새로운 소셜 회원 가입: {}", email);

        return userRepository.save(User.builder()
                .email(email)
                .nickname(uniqueNickname)
                .password(null) // 소셜은 비번 없음
                .age(null)      // 추후 입력 유도
                .gender(null)
                .termsOfServiceAgreedAt(LocalDateTime.now()) // 자동 동의 처리
                .privacyPolicyAgreedAt(LocalDateTime.now())
                .marketingConsentAgreedAt(null)
                .build());
    }
}