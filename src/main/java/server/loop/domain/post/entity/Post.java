package server.loop.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

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

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시간

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시간

    @Builder
    public Post(User author, String title, String content) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    // 수정 기능을 위한 메소드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}