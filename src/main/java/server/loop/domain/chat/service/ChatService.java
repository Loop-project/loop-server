package server.loop.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.chat.dto.req.ChatMessageSendRequest;
import server.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import server.loop.domain.chat.dto.res.ChatMessageResponse;
import server.loop.domain.chat.dto.res.ChatRoomMemberResponse;
import server.loop.domain.chat.dto.res.ChatRoomResponse;
import server.loop.domain.chat.entity.ChatMessage;
import server.loop.domain.chat.entity.ChatRoom;
import server.loop.domain.chat.entity.ChatRoomMember;
import server.loop.domain.chat.entity.repository.ChatMessageRepository;
import server.loop.domain.chat.entity.repository.ChatRoomMemberRepository;
import server.loop.domain.chat.entity.repository.ChatRoomRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ==== Room ====

    @Transactional
    public ChatRoomResponse createRoom(UserDetails userDetails, ChatRoomCreateRequest req) {
        User owner = currentUser(userDetails);
        String id = UUID.randomUUID().toString();

        ChatRoom room = ChatRoom.builder()
                .id(id)
                .title(req.getTitle().trim())
                .visibility(normalizeVisibility(req.getVisibility()))
                .owner(owner)
                .build();
        ChatRoom savedRoom = chatRoomRepository.save(room);

        // owner 자동 가입
        memberRepository.save(ChatRoomMember.builder()
                .room(savedRoom).user(owner).role("OWNER").build());

        return toRoomResponse(savedRoom, owner, true);
    }

    @Transactional
    public void joinPublicRoom(UserDetails userDetails, String roomId) {
        User user = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(roomId);

        if (!"PUBLIC".equals(room.getVisibility())) {
            throw new IllegalStateException("공개방이 아닙니다. 초대/승인이 필요합니다.");
        }
        if (!memberRepository.existsByRoomAndUser(room, user)) {
            memberRepository.save(ChatRoomMember.builder()
                    .room(room).user(user).role("MEMBER").build());
        }
    }

    @Transactional
    public void leaveRoom(UserDetails userDetails, String roomId) {
        User user = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(roomId);

        memberRepository.findByRoomAndUser(room, user)
                .ifPresent(memberRepository::delete);

        // 정책적으로 방장 처리/방 삭제 자동화가 필요하면 여기에 추가
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> listPublicRooms(UserDetails userDetails, int page, int size) {
        User me = currentUserOrNull(userDetails); // 공개 API: null 허용
        Page<ChatRoom> pages = chatRoomRepository.findByVisibility("PUBLIC", PageRequest.of(page, size));
        return pages.map(r -> {
            boolean joined = (me != null) && memberRepository.existsByRoomAndUser(r, me);
            return toRoomResponse(r, me, joined);
        });
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> listMyRooms(UserDetails userDetails) {
        User me = currentUser(userDetails);
        return memberRepository.findByUser(me).stream()
                .map(m -> toRoomResponse(m.getRoom(), me, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(UserDetails userDetails, String roomId) {
        User me = currentUserOrNull(userDetails);
        ChatRoom room = getRoomOrThrow(roomId);
        boolean joined = (me != null) && memberRepository.existsByRoomAndUser(room, me);
        return toRoomResponse(room, me, joined);
    }

    // ==== Message ====

    @Transactional
    public ChatMessageResponse sendMessage(UserDetails userDetails, ChatMessageSendRequest req) {
        User sender = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(req.getRoomId());

        // 멤버십 검증(공개방이라도 "보내기"는 가입자만 허용)
        if (!memberRepository.existsByRoomAndUser(room, sender)) {
            throw new IllegalStateException("해당 채팅방의 멤버가 아닙니다.");
        }

        ChatMessage saved = messageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(req.getContent())
                .type(req.getType() == null ? "TEXT" : req.getType())
                .build());

        ChatMessageResponse payload = toMessageResponse(saved);
        // 공개방은 토픽 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat." + room.getId(), payload);
        return payload;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(UserDetails userDetails, String roomId, int size, Long beforeId) {
        User me = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(roomId);

        // 읽기 권한: 현재 정책은 멤버만 허용 (공개 읽기 원하면 여기 완화)
        if (!memberRepository.existsByRoomAndUser(room, me)) {
            throw new IllegalStateException("해당 채팅방의 멤버가 아닙니다.");
        }

        PageRequest pr = PageRequest.of(0, Math.min(size, 100));
        var list = (beforeId == null)
                ? messageRepository.findByRoom_IdOrderByIdDesc(roomId, pr)
                : messageRepository.findByRoom_IdAndIdLessThanOrderByIdDesc(roomId, beforeId, pr);

        return list.stream().map(this::toMessageResponse).toList();
    }

    // ==== Helpers ====

    private User currentUser(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        String email = userDetails.getUsername(); // CustomUserDetailsService에서 username=email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다. email=" + email));
    }

    private User currentUserOrNull(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return null;
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email).orElse(null);
    }

    private ChatRoom getRoomOrThrow(String roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. roomId=" + roomId));
    }

    private String normalizeVisibility(String v) {
        return (v == null || v.isBlank()) ? "PUBLIC" : v.toUpperCase();
    }

    private ChatRoomResponse toRoomResponse(ChatRoom r, User me, boolean joined) {
        long memberCount = memberRepository.countByRoom(r);
        List<ChatRoomMemberResponse> members = memberRepository.findByRoom(r).stream()
                .map(member -> ChatRoomMemberResponse.builder()
                        .userId(member.getUser().getId())
                        .nickname(member.getUser().getNickname())
                        .build())
                .toList();

        return ChatRoomResponse.builder()
                .roomId(r.getId())
                .title(r.getTitle())
                .visibility(r.getVisibility())
                .ownerId(r.getOwner().getId())
                .memberCount(memberCount)
                .createdAt(r.getCreatedAt().toEpochMilli())
                .joined(joined)
                .members(members)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .messageId(m.getId())
                .roomId(m.getRoom().getId())
                .senderId(m.getSender().getId())
                .senderNickname(m.getSender().getNickname())
                .content(m.getContent())
                .type(m.getType())
                .createdAt(m.getCreatedAt().toEpochMilli())
                .build();
    }
}
