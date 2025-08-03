package server.loop.domain.post.dto.post.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시글 좋아요 정보 응답")
public class PostLikeResponseDto {
    @Schema(description = "유저")
    private boolean likedByUser;
    @Schema(description = "좋아요 갯수")
    private int likeCount;
}