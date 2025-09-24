package server.loop.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.chat.dto.req.ChatMessageSendRequest;
import server.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import server.loop.domain.chat.dto.res.ChatMessageResponse;
import server.loop.domain.chat.dto.res.ChatRoomResponse;
import server.loop.domain.chat.entity.ChatMessage;
import server.loop.domain.chat.entity.ChatRoom;
import server.loop.domain.chat.entity.ChatRoomMember;
import server.loop.domain.chat.entity.repository.ChatMessageRepository;
import server.loop.domain.chat.entity.repository.ChatRoomMemberRepository;
import server.loop.domain.chat.entity.repository.ChatRoomRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.security.Principal;
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
    public ChatRoomResponse createRoom(Principal principal, ChatRoomCreateRequest req) {
        User owner = currentUser(principal);
        String id = UUID.randomUUID().toString();

        ChatRoom room = ChatRoom.builder()
                .id(id)
                .title(req.getTitle().trim())
                .visibility(normalizeVisibility(req.getVisibility()))
                .owner(owner)
                .build();
        chatRoomRepository.save(room);

        // owner 자동 가입
        memberRepository.save(ChatRoomMember.builder()
                .room(room).user(owner).role("OWNER").build());

        return toRoomResponse(room, owner, true);
    }

    @Transactional
    public void joinPublicRoom(Principal principal, String roomId) {
        User user = currentUser(principal);
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
    public void leaveRoom(Principal principal, String roomId) {
        User user = currentUser(principal);
        ChatRoom room = getRoomOrThrow(roomId);

        memberRepository.findByRoomAndUser(room, user)
                .ifPresent(memberRepository::delete);

        // 방장은 나갈 수 없게 할지, 위임 후 나가게 할지 정책에 따라 변경
        // 여기서는 방장도 나갈 수 있게 두되, 방 멤버가 0명이면 방을 지우는 옵션을 넣을 수도 있음(생략)
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> listPublicRooms(Principal principal, int page, int size) {
        User me = currentUser(principal);
        Page<ChatRoom> pages = chatRoomRepository.findByVisibility("PUBLIC", PageRequest.of(page, size));
        return pages.map(r -> toRoomResponse(r, me, memberRepository.existsByRoomAndUser(r, me)));
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> listMyRooms(Principal principal) {
        User me = currentUser(principal);
        return memberRepository.findByUser(me).stream()
                .map(m -> toRoomResponse(m.getRoom(), me, true))
                .toList();
    }

    // ==== Message ====
    @Transactional
    public ChatMessageResponse sendMessage(Principal principal, ChatMessageSendRequest req) {
        User sender = currentUser(principal);
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
    public List<ChatMessageResponse> getMessages(Principal principal, String roomId, int size, Long beforeId) {
        User me = currentUser(principal);
        ChatRoom room = getRoomOrThrow(roomId);

        // 읽기 권한: 공개방 → 누구나 읽기 허용으로 바꿔도 됨(지금은 멤버만 읽기)
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
    private User currentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        String email = principal.getName(); // StompAuthChannelInterceptor에서 email을 username으로 셋업
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다. email=" + email));
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
        return ChatRoomResponse.builder()
                .roomId(r.getId())
                .title(r.getTitle())
                .visibility(r.getVisibility())
                .ownerId(r.getOwner().getId())
                .memberCount(memberCount)
                .createdAt(r.getCreatedAt().toEpochMilli())
                .joined(joined)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .messageId(m.getId())
                .roomId(m.getRoom().getId())
                .senderId(m.getSender().getId())
                .senderNickname(m.getSender().getNickname()) // 프로젝트 필드명에 맞게
                .content(m.getContent())
                .type(m.getType())
                .createdAt(m.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                .build();
    }
}