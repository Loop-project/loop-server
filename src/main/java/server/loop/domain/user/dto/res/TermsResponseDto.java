package server.loop.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import server.loop.domain.user.entity.Terms;
import server.loop.domain.user.entity.TermsType;

@Getter
@Schema(description = "약관 정보 응답")
public class TermsResponseDto {
    @Schema(description = "약관 종류")
    private final TermsType type;
    @Schema(description = "약관 내용")
    private final String content;
    @Schema(description = "약관 버전")
    private final String version;

    public TermsResponseDto(Terms terms) {
        this.type = terms.getType();
        this.content = terms.getContent();
        this.version = terms.getVersion();
    }
}