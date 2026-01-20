package server.loop.domain.post.dto.post.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import server.loop.domain.post.entity.Category;

import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 수정 요청")
public class PostUpdateRequestDto {

    @Schema(description = "수정할 게시글 제목", example = "수정된 제목입니다.")
    private String title;

    @Schema(description = "수정할 게시글 내용", example = "내용을 수정했습니다.")
    private String content;

    @Schema(description = "수정할 카테고리", example = "USED")
    private Category category;

    @Schema(description = "삭제할 사진 List", example = "String List")
    private List<Long> deleteImageIds;
}