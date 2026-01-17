package server.loop.domain.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminBanUserRequest {
    @NotBlank(message = "정지 사유는 필수입니다.")
    private String reason;
}
