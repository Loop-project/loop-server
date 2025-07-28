package server.loop.domain.post.dto.comment.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequestDto {
    private Long postId;
    private Long parentId; // 대댓글일 경우 부모 댓글의 ID, 일반 댓글이면 null
    private String content;
}