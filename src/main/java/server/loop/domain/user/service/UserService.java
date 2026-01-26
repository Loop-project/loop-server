package server.loop.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.auth.entity.RefreshToken;
import server.loop.domain.auth.entity.repo.RefreshTokenRepository;
import server.loop.domain.user.dto.req.PasswordUpdateRequestDto;
import server.loop.domain.user.dto.req.UserLoginDto;
import server.loop.domain.user.dto.req.UserSignUpDto;
import server.loop.domain.user.dto.req.UserUpdateRequestDto;
import server.loop.domain.user.dto.res.UserResponseDto;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.common.error.ErrorCode;
import server.loop.global.common.exception.CustomException;
import server.loop.global.security.JwtTokenProvider;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public Long signUp(UserSignUpDto signUpDto) {
        log.info("[SignUp] Request email: {}, nickname: {}", signUpDto.getEmail(), signUpDto.getNickname());
        // 이메일 중복 체크
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            log.warn("[SignUp] Email already exists: {}", signUpDto.getEmail());
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        //닉네임 중복 체크
        if (userRepository.findByNickname(signUpDto.getNickname()).isPresent()) {
            log.warn("[SignUp] Nickname already exists: {}", signUpDto.getNickname());
            throw new CustomException(ErrorCode.ALREADY_EXISTS, "이미 존재하는 닉네임입니다.");
        }
        if (!signUpDto.isAgreedToTermsOfService() || !signUpDto.isAgreedToPrivacyPolicy()) {
            throw new CustomException(ErrorCode.INVALID_BODY, "필수 약관에 동의해야 합니다.");
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
        log.info("[SignUp] User registered successfully. ID: {}", user.getId());
        return user.getId();
    }

    //로그인
    public TokenDto login(UserLoginDto loginDto) {
        log.info("[Login] Request email: {}", loginDto.getEmail());
        User user = userRepository.findByEmailAndDeletedAtIsNull(loginDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_USER, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        (token) -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken))
                );

        log.info("[Login] Success email: {}", user.getEmail());
        return new TokenDto(accessToken, refreshToken);
    }


    //회원 조회
    @Transactional(readOnly = true) // 조회만 하므로 readOnly = true 추가
    public UserResponseDto getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.: " + email));

        // User 엔티티를 UserResponseDto로 변환하여 반환
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }

    //회원 정보 수정
    public void updateUser(UserUpdateRequestDto requestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (requestDto.getNickname() != null && !requestDto.getNickname().isBlank()) {
            if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
                throw new CustomException(ErrorCode.ALREADY_EXISTS, "이미 존재하는 닉네임입니다.");
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteByUserId(user.getId());

        user.withdraw();                  // 필드 변경
        userRepository.saveAndFlush(user); // 강제 flush로 DB까지 반영 확인
    }

    //닉네임 변경
    public void updateNickname(String email, String newNickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "해당 사용자가 존재하지 않습니다."));
        if (newNickname == null || newNickname.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_NAME, "닉네임은 비어 있을 수 없습니다.");
        }
        user.setNickname(newNickname);
    }
    //비밀번호 변경
    public void updatePassword(String username, PasswordUpdateRequestDto dto) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "해당 사용자가 존재하지 않습니다."));

        // 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }

}