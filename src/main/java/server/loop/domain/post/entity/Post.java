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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @Column(name = "report_count", nullable = false)
    private int reportCount = 0; // 신고 횟수

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시간

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시간

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; //삭제
    @Builder
    public Post(User author, Category category, String title, String content) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void update(Category category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void addReport() {
        this.reportCount++;
    }
    public void softDelete() {
        this.isDeleted = true;
    }
}