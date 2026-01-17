package server.loop.domain.report.dto.report.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserReportRequestDto {
    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;
}
