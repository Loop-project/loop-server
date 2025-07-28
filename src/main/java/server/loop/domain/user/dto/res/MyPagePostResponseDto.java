package server.loop.domain.user.dto.res;

import lombok.Getter;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;

@Getter
public class MyPagePostResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;
    private final Category category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final int likeCount;
    private final int commentCount;

    public MyPagePostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorNickname = post.getAuthor().getNickname();
        this.category = post.getCategory();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikes().size();
        this.commentCount = post.getComments().size();
    }
}