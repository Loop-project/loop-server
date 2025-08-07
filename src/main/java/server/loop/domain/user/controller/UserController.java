package server.loop.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.user.dto.req.UpdateNicknameRequest;
import server.loop.domain.user.dto.req.UserLoginDto;
import server.loop.domain.user.dto.req.UserSignUpDto;
import server.loop.domain.user.dto.req.UserUpdateRequestDto;
import server.loop.domain.user.dto.res.UserResponseDto;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.domain.user.service.UserService;

import java.util.Map;

@Tag(name = "User", description = "사용자 인증 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임 등으로 사용자를 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody UserSignUpDto userSignUpDto) throws Exception {
        userService.signUp(userSignUpDto);
        return ResponseEntity.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));
    }

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인하고 Access/Refresh 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserLoginDto loginDto) { // 반환 타입 변경
        TokenDto tokenDto = userService.login(loginDto);
        return ResponseEntity.ok(tokenDto);
    }
    @Operation(summary = "닉네임 변경", description = "NickName 변경")
    @PatchMapping("/profile/nickname")
    public ResponseEntity<Map<String, String>> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateNicknameRequest request) {

        userService.updateNickname(userDetails.getUsername(), request.getNickname());
        return ResponseEntity.ok(Map.of("message", "닉네임이 변경되었습니다."));
    }
    //닉네임 중복 체크
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 체크 True/False")
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isAvailable = !userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("available: ", isAvailable));
    }


    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponseDto userInfo = userService.getUserInfoByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userInfo);
    }
    //정보 수정
    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 닉네임 또는 비밀번호를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<String> updateUser(@RequestBody UserUpdateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        userService.updateUser(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    //회원 삭제
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 탈퇴(Soft Delete) 처리합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}