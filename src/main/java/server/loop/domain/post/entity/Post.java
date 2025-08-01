package server.loop.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import server.loop.domain.user.entity.User;
import server.loop.global.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id; // 고유 ID

    @ManyToOne // 게시글(Many)과 작성자(One)의 관계
    @JoinColumn(name = "user_id") // 외래키 이름 지정
    private User author; // 작성자 (User Entity 참조)

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(name = "category", nullable = false)
    private Category category; // 카테고리

    @Column(name = "title", nullable = false)
    private String title; // 제목

    @Column(name = "content", nullable = false)
    private String content; // 내용

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "report_count", nullable = false)
    private int reportCount = 0; // 신고 횟수

    @Builder
    public Post(User author, Category category, String title, String content) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    // === 연관관계 편의 메서드 ===
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }
    public void setAuthor(User author) {
        this.author = author;
    }
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void addLike(PostLike like) {
        likes.add(like);
        like.setPost(this);
    }

    // === 비즈니스 로직 ===
    public boolean isLikedBy(User user) {
        return user != null && likes.stream().anyMatch(like -> like.getUser().getId().equals(user.getId()));
    }

    public void update(Category category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void addReport() {
        this.reportCount++;
    }
}