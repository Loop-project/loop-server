package server.loop.domain.post.dto.comment.res;

import lombok.Getter;
import server.loop.domain.post.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponseDto {
    private final Long commentId;
    private final String content;
    private final String authorNickname;
    private final LocalDateTime createdAt;
    private final List<CommentResponseDto> replies; // 대댓글 목록

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.authorNickname = comment.getAuthor().getNickname();
        this.createdAt = comment.getCreatedAt();
        // 자식 댓글들을 DTO로 변환
        this.replies = comment.getChildren().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}