package server.loop.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import server.loop.domain.user.entity.User;

import java.time.Instant;

@Entity
@Table(name = "chat_room_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_room_user", columnNames = {"room_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String role; // OWNER | MEMBER

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;
}