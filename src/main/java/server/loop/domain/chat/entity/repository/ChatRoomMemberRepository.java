package server.loop.domain.chat.entity.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.chat.entity.ChatRoom;
import server.loop.domain.chat.entity.ChatRoomMember;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsByRoomAndUser(ChatRoom room, User user);
    Optional<ChatRoomMember> findByRoomAndUser(ChatRoom room, User user);
    Optional<ChatRoomMember> findByRoom_PostAndUser(Post post, User user);
    long countByRoom(ChatRoom room);
    List<ChatRoomMember> findByUser(User user);
    List<ChatRoomMember> findByRoom(ChatRoom room);
}