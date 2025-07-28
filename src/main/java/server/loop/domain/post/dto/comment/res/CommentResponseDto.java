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

    @Schema(description = "생성 시간", example = "2025-07-28T18:00:55.753302")
    private final LocalDateTime createdAt;

    @Schema(description = "대댓글 목록 (계층 구조)")
    private final List<CommentResponseDto> replies;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.createdAt = comment.getCreatedAt();

        // 작성자가 null이거나 탈퇴한 경우 처리
        User author = comment.getAuthor();
        this.authorNickname = (author == null || author.getDeletedAt() != null)
                ? "탈퇴한 사용자"
                : author.getNickname();

        // 댓글이 삭제된 경우 처리
        if (comment.isDeleted()) {
            this.content = "삭제된 댓글입니다.";
            this.replies = List.of(); // 빈 리스트
        } else {
            this.content = comment.getContent();
            // 자식 댓글들(대댓글)도 DTO로 변환
            this.replies = comment.getChildren().stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }
}