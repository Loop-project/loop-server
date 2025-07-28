package server.loop.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.auth.dto.TokenDto;
import server.loop.domain.user.dto.req.UserLoginDto;
import server.loop.domain.user.dto.req.UserSignUpDto;
import server.loop.domain.user.dto.req.UserUpdateRequestDto;
import server.loop.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserSignUpDto userSignUpDto) throws Exception {
        userService.signUp(userSignUpDto);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserLoginDto loginDto) { // 반환 타입 변경
        TokenDto tokenDto = userService.login(loginDto);
        return ResponseEntity.ok(tokenDto);
    }

    //정보 수정
    @PatchMapping("/me")
    public ResponseEntity<String> updateUser(@RequestBody UserUpdateRequestDto requestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        userService.updateUser(requestDto, userDetails.getUsername());
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    //회원 삭제
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}