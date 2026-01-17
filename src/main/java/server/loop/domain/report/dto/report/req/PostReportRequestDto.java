package server.loop.domain.report.dto.report.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "게시글 신고 요청")
public class PostReportRequestDto {

    @Schema(description = "신고 사유", example = "부적절한 내용이 포함되어 있습니다.")
    private String reason;
}