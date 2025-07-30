package server.loop.domain.post.dto.comment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "댓글 수정 요청")
public class CommentUpdateRequestDto {

    @Schema(description = "수정할 댓글 내용", example = "내용을 수정했습니다.")
    private String content;
}