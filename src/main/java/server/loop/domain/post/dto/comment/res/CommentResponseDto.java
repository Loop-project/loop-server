package server.loop.domain.post.dto.comment.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "댓글 정보 응답")
public class CommentResponseDto {

    @Schema(description = "댓글 ID", example = "10")
    private final Long commentId;

    @Schema(description = "댓글 내용 (삭제된 경우 '삭제된 댓글입니다.' 표시)", example = "좋은 글 감사합니다.")
    private final String content;

    @Schema(description = "작성자 닉네임 (탈퇴 시 '탈퇴한 사용자' 표시)", example = "루프유저")
    private final String authorNickname;

    @Schema(description = "작성자 ID", example = "1")
    private final Long authorId;

    @Schema(description = "생성 시간", example = "2025-07-28T18:00:55.753302")
    private final LocalDateTime createdAt;

    @Schema(description = "대댓글 목록 (계층 구조)")
    private final List<CommentResponseDto> replies;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.createdAt = comment.getCreatedAt();

        User author = comment.getAuthor();
        if (author == null || author.getDeletedAt() != null) {
            this.authorNickname = "탈퇴한 사용자";
            this.authorId = null;
        } else {
            this.authorNickname = author.getNickname();
            this.authorId = author.getId();   // 작성자 ID 세팅
        }

        if (comment.isDeleted()) {
            this.content = "삭제된 댓글입니다.";
            this.replies = List.of();
        } else {
            this.content = comment.getContent();
            this.replies = comment.getChildren().stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }
}