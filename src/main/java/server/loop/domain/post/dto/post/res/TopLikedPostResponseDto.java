package server.loop.domain.post.dto.post.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TopLikedPostResponseDto {
    private Long id;
    private String title;
    private String authorNickname;
    private int likeCount;
    private LocalDateTime createdAt;

    public static TopLikedPostResponseDto fromEntity(Post post) {
        return TopLikedPostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorNickname(post.getAuthor().getNickname())  // 작성자 닉네임
                .likeCount(post.getLikes().size())               // 좋아요 개수
                .createdAt(post.getCreatedAt())                  // 작성일
                .build();
    }
}
