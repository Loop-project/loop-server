package server.loop.domain.post.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "댓글 생성 이벤트")
public class CommentCreatedEvent {

    @Schema(description = "발신자(댓글 작성자) ID", example = "1")
    private final Long senderId;

    @Schema(description = "수신자(알림 받을 사람) ID", example = "2")
    private final Long receiverId;

    @Schema(description = "게시글 ID", example = "10")
    private final Long postId;

    @Schema(description = "댓글 ID", example = "100")
    private final Long commentId;

    @Schema(description = "게시글 제목", example = "오늘 날씨 좋네요")
    private final String postTitle;

    @Schema(description = "알림 메시지", example = "Your post has a new comment.")
    private final String message;

    public CommentCreatedEvent(Long senderId, Long receiverId, Long postId, Long commentId, String postTitle, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.postId = postId;
        this.commentId = commentId;
        this.postTitle = postTitle;
        this.message = message;
    }
}
