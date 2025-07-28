package server.loop.domain.user.dto.req;

import lombok.Getter;
import lombok.Setter;
import server.loop.domain.user.entity.Gender;

@Getter
@Setter
public class UserSignUpDto {
    private String email;
    private String password;
    private String nickname;
    private Integer age;
    private Gender gender;
}