package server.loop.domain.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import server.loop.domain.user.entity.User;

import java.time.Instant;

@Entity
@Table(name = "chat_room")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom {
    @Id
    @Column(length = 100)
    private String id; // UUID 문자열

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 10)
    private String visibility; // PUBLIC | PRIVATE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}