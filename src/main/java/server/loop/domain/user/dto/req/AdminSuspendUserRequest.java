package server.loop.domain.user.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminSuspendUserRequest {
    @Min(value = 1, message = "정지 일수는 최소 1일이어야 합니다.")
    private int days;

    @NotBlank(message = "정지 사유는 필수입니다.")
    private String reason;
}
