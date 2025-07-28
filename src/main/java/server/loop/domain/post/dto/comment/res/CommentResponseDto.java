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
    private final List<CommentResponseDto> replies;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.authorNickname = comment.getAuthor().getNickname();
        this.createdAt = comment.getCreatedAt();

        if(comment.isDeleted()) {
            this.content = "삭제된 댓글입니다.";
            this.replies = List.of(); // 빈 리스트
        } else {
            this.content = comment.getContent();
            this.replies = comment.getChildren().stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }
}