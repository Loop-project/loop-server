package server.loop.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import server.loop.domain.user.entity.Gender;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class UserSignUpDto {

    @Schema(description = "이메일", example = "test@loop.com")
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    private String password;

    @Schema(description = "닉네임", example = "루프유저")
    private String nickname;

    @Schema(description = "나이", example = "25")
    private Integer age;

    @Schema(description = "성별 (MALE 또는 FEMALE)", example = "MALE")
    private Gender gender;
}