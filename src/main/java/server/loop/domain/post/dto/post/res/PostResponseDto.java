package server.loop.domain.post.dto.post.res;

import lombok.Getter;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;
    private final Category category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final int likeCount;
    private final int commentCount;
    private final boolean likedByUser;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorNickname = post.getAuthor().getNickname(); // 작성자 닉네임
        this.category = post.getCategory();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikes().size();
        this.commentCount = post.getComments().size();
        this.likedByUser = false;
    }

    //상세 조회용
    public PostResponseDto(Post post, boolean likedByUser) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorNickname = post.getAuthor().getNickname();
        this.category = post.getCategory();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikes().size();
        this.commentCount = post.getComments().size();
        this.likedByUser = likedByUser;
    }
}