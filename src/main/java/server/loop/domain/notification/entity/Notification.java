package server.loop.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.user.entity.User;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    private String message;

    @Column(nullable = false)
    private boolean isRead;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String postTitle;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
