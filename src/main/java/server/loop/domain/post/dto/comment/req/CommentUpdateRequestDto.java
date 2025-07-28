package server.loop.domain.post.dto.comment.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequestDto {
    private String content;
}