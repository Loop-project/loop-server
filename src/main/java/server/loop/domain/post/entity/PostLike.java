package server.loop.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import server.loop.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 한 사용자는 한 게시글에 한 번만 좋아요를 누를 수 있도록 복합 유니크 키 설정
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}