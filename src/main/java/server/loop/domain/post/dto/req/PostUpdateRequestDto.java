package server.loop.domain.post.dto.req;

import lombok.Getter;
import lombok.Setter;
import server.loop.domain.post.entity.Category;

@Getter
@Setter
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private Category category;
}