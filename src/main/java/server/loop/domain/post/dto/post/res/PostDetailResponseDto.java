package server.loop.domain.post.dto.post.res;

import lombok.Getter;
import server.loop.domain.post.dto.comment.res.CommentResponseDto;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostDetailResponseDto {
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
    private final List<CommentResponseDto> comments;

    public PostDetailResponseDto(Post post, boolean likedByUser) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorNickname = (post.getAuthor() == null || post.getAuthor().isDeleted())
                ? "탈퇴한 회원입니다."
                : post.getAuthor().getNickname();
        this.category = post.getCategory();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = (post.getLikes() != null) ? post.getLikes().size() : 0;
        this.commentCount = (post.getComments() != null) ? post.getComments().size() : 0;
        this.likedByUser = likedByUser;
        this.comments = (post.getComments() != null)
                ? post.getComments().stream()
                .filter(comment -> comment.getParent() == null) // 최상위 댓글만
                .map(CommentResponseDto::new)
                .collect(Collectors.toList())
                : List.of();
    }
}