package server.loop.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Schema(description = "마이페이지 게시글 목록 응답")
public class MyPagePostResponseDto {

    @Schema(description = "게시글 ID", example = "1")
    private final Long id;

    @Schema(description = "제목", example = "마이페이지에서 조회한 게시글")
    private final String title;

    @Schema(description = "내용", example = "마이페이지 게시글 내용입니다.")
    private final String content;

    @Schema(description = "작성자 닉네임", example = "루프유저")
    private final String authorNickname;

    @Schema(description = "카테고리", example = "FREE")
    private final Category category;

    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;

    @Schema(description = "마지막 수정 시간")
    private final LocalDateTime updatedAt;

    @Schema(description = "좋아요 개수", example = "10")
    private final int likeCount;

    @Schema(description = "댓글 개수", example = "5")
    private final int commentCount;

    public MyPagePostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();

        User author = post.getAuthor();
        this.authorNickname = (author == null || author.getDeletedAt() != null)
                ? "탈퇴한 사용자"
                : author.getNickname();

        this.category = post.getCategory();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikes().size();
        this.commentCount = post.getComments().size();
    }
}