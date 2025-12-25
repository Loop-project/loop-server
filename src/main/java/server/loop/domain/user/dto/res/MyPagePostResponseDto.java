package server.loop.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 게시글 목록 응답")
public record MyPagePostResponseDto(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "마이페이지에서 조회한 게시글")
        String title,

        @Schema(description = "내용", example = "마이페이지 게시글 내용입니다.")
        String content,

        @Schema(description = "작성자 닉네임", example = "루프유저")
        String authorNickname,

        @Schema(description = "카테고리", example = "FREE")
        Category category,

        @Schema(description = "생성 시간")
        LocalDateTime createdAt,

        @Schema(description = "마지막 수정 시간")
        LocalDateTime updatedAt,

        @Schema(description = "좋아요 개수", example = "10")
        int likeCount,

        @Schema(description = "댓글 개수", example = "5")
        int commentCount
) {
    public static MyPagePostResponseDto from(Post post) {
        User author = post.getAuthor();
        String nickname = (author == null || author.getDeletedAt() != null)
                ? "탈퇴한 사용자"
                : author.getNickname();

        return new MyPagePostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.getCategory(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getLikes().size(),
                post.getComments().size()
        );
    }
}