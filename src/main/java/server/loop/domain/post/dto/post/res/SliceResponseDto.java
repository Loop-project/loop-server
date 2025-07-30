package server.loop.domain.post.dto.post.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Schema(description = "페이지네이션 응답 (무한 스크롤)")
public class SliceResponseDto<T> {

    @Schema(description = "조회된 데이터 목록")
    private final List<T> content;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private final int currentPage;

    @Schema(description = "페이지 당 데이터 개수", example = "20")
    private final int size;

    public SliceResponseDto(Slice<T> slice) {
        this.content = slice.getContent();
        this.hasNext = slice.hasNext();
        this.currentPage = slice.getNumber();
        this.size = slice.getSize();
    }
}