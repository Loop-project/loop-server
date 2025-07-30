package server.loop.domain.post.dto.comment.req;

import lombok.Getter;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "댓글/대댓글 생성 요청")
public class CommentCreateRequestDto {

    @Schema(description = "댓글을 작성할 게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "대댓글의 경우 부모 댓글 ID (일반 댓글은 null 또는 생략)", example = "1")
    private Long parentId;

    @Schema(description = "댓글 내용", example = "정말 유용한 정보네요!")
    private String content;
}