package server.loop.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import server.loop.domain.user.entity.Terms;
import server.loop.domain.user.entity.TermsType;

@Schema(description = "약관 정보 응답")
public record TermsResponseDto(
        @Schema(description = "약관 종류")
        TermsType type,
        @Schema(description = "약관 내용")
        String content,
        @Schema(description = "약관 버전")
        String version
) {
    public static TermsResponseDto from(Terms terms) {
        return new TermsResponseDto(terms.getType(), terms.getContent(), terms.getVersion());
    }
}