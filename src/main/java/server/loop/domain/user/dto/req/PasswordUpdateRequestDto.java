package server.loop.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 수정")
public class PasswordUpdateRequestDto {

    @Schema(description = "기존 비밀번호")
    @NotBlank(message = "기존 비밀번호는 필수입니다.")
    private String currentPassword;

    @Schema(description = "수정할 비밀번호")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
