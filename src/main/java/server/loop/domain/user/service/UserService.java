package server.loop.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.auth.entity.RefreshToken;
import server.loop.domain.auth.entity.repo.RefreshTokenRepository;
import server.loop.domain.user.dto.req.UserLoginDto;
import server.loop.domain.user.dto.req.UserSignUpDto;
import server.loop.domain.user.dto.req.UserUpdateRequestDto;
import server.loop.domain.user.dto.res.UserResponseDto;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.security.JwtTokenProvider;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public Long signUp(UserSignUpDto signUpDto) throws Exception {
        // 이메일 중복 체크
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }
        //닉네임 중복 체크
        if (userRepository.findByNickname(signUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }
        if (!signUpDto.isAgreedToTermsOfService() || !signUpDto.isAgreedToPrivacyPolicy()) {
            throw new IllegalArgumentException("필수 약관에 동의해야 합니다.");
        }
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .nickname(signUpDto.getNickname())
                .age(signUpDto.getAge())
                .gender(signUpDto.getGender())
                .termsOfServiceAgreedAt(now) // 필수 약관 동의 시간 기록
                .privacyPolicyAgreedAt(now)  // 필수 약관 동의 시간 기록
                .marketingConsentAgreedAt(signUpDto.isAgreedToMarketing() ? now : null) // 선택 약관 처리
                .build();

        userRepository.save(user);
        return user.getId();
    }

    //로그인
    public TokenDto login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 1. Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 2. Refresh Token을 DB에 저장 (이미 있으면 업데이트, 없으면 새로 생성)
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        (token) -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken))
                );

        // 3. 두 토큰을 DTO에 담아 반환
        return new TokenDto(accessToken, refreshToken);
    }

    //회원 조회
    @Transactional(readOnly = true) // 조회만 하므로 readOnly = true 추가
    public UserResponseDto getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.: " + email));

        // User 엔티티를 UserResponseDto로 변환하여 반환
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }

    //회원 정보 수정
    public void updateUser(UserUpdateRequestDto requestDto, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (requestDto.getNickname() != null && !requestDto.getNickname().isBlank()) {
            if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
                throw new Exception("이미 존재하는 닉네임입니다.");
            }
            user.updateNickname(requestDto.getNickname());
        }
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(requestDto.getPassword()));
        }
    }

    //회원 탈퇴
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // Refresh Token 삭제
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        // User Soft Delete
        user.withdraw();
        userRepository.save(user);
    }
}