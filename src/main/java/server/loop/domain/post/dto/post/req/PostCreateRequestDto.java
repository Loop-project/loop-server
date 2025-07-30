package server.loop.domain.post.dto.post.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import server.loop.domain.post.entity.Category;

@Getter
@Setter
@Schema(description = "게시글 생성 요청")
public class PostCreateRequestDto {

    @Schema(description = "게시글 제목", example = "새로운 게시글 제목입니다.")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 여기에 글을 작성해주세요.")
    private String content;

    @Schema(description = "카테고리", example = "FREE")
    private Category category;
}