package server.loop.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import server.loop.domain.chat.event.ChatMessageSavedEvent;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostRepository postRepository;

    // ==== Room ====

    @Transactional
    public ChatRoomResponse createRoom(UserDetails userDetails, ChatRoomCreateRequest req) {
        User owner = currentUser(userDetails);
        String id = UUID.randomUUID().toString();
        log.info("[CreateChatRoom] title={}, owner={}", req.getTitle(), owner.getEmail());

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
        log.info("[JoinChatRoom] roomId={}, user={}", roomId, user.getEmail());

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
        log.info("[LeaveChatRoom] roomId={}, user={}", roomId, user.getEmail());

        memberRepository.findByRoomAndUser(room, user)
                .ifPresent(memberRepository::delete);
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
        List<ChatRoomMember> memberships = memberRepository.findByUser(me);
        log.debug("DEBUG: User {} ({}) has {} memberships.", me.getId(), me.getNickname(), memberships.size());
        
        return memberships.stream()
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

    @Transactional
    public ChatRoomResponse startPrivateChat(UserDetails userDetails, Long postId) {
        User me = currentUser(userDetails);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.getCategory() != Category.USED) {
            throw new IllegalStateException("중고 거래 게시글에서만 1:1 채팅이 가능합니다.");
        }

        User seller = post.getAuthor();
        log.info("[StartPrivateChat] buyer={}, seller={}, postId={}", me.getEmail(), seller.getEmail(), postId);

        if (me.getId().equals(seller.getId())) {
            throw new IllegalArgumentException("자신의 게시글과는 채팅할 수 없습니다.");
        }

        return memberRepository.findByRoom_PostAndUser(post, me)
                .map(member -> toRoomResponse(member.getRoom(), me, true))
                .orElseGet(() -> createUsedChatRoom(me, seller, post));
    }

    private ChatRoomResponse createUsedChatRoom(User buyer, User seller, Post post) {
        String id = UUID.randomUUID().toString();
        ChatRoom room = ChatRoom.builder()
                .id(id)
                .title(post.getTitle())
                .visibility("PRIVATE")
                .owner(buyer)
                .post(post)
                .build();
        
        ChatRoom savedRoom = chatRoomRepository.save(room);

        memberRepository.save(ChatRoomMember.builder()
                .room(savedRoom).user(buyer).role("OWNER").build());
        
        memberRepository.save(ChatRoomMember.builder()
                .room(savedRoom).user(seller).role("MEMBER").build());

        return toRoomResponse(savedRoom, buyer, true);
    }

    // ==== Message ====

    @Transactional
    public ChatMessageResponse sendMessage(UserDetails userDetails, ChatMessageSendRequest req) {
        User sender = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(req.getRoomId());

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
        eventPublisher.publishEvent(new ChatMessageSavedEvent(room.getId(), payload));
        log.info("[SendMessage] roomId={}, sender={}, type={}", room.getId(), sender.getEmail(), req.getType());
        return payload;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(UserDetails userDetails, String roomId, int size, Long beforeId) {
        User me = currentUser(userDetails);
        ChatRoom room = getRoomOrThrow(roomId);

        if (!memberRepository.existsByRoomAndUser(room, me)) {
            throw new IllegalStateException("해당 채팅방의 멤버가 아닙니다.");
        }

        PageRequest pr = PageRequest.of(0, Math.min(size, 100));
        List<ChatMessage> list = messageRepository.findMessages(roomId, beforeId, pr);

        return list.stream().map(this::toMessageResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> listMyRooms(UserDetails userDetails, Pageable pageable) {
        User me = currentUser(userDetails);

        // 페이징된 멤버십 목록 가져오기
        Page<ChatRoomMember> pages = memberRepository.findByUser(me, pageable);

        // Page<Member> -> Page<Response> 로 변환
        return pages.map(m -> toRoomResponse(m.getRoom(), me, true));
    }

    // ==== Helpers ====

    private User currentUser(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        String email = userDetails.getUsername(); 
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

        Long postId = r.getPost() != null ? r.getPost().getId() : null;

        return ChatRoomResponse.builder()
                .roomId(r.getId())
                .title(r.getTitle())
                .visibility(r.getVisibility())
                .ownerId(r.getOwner().getId())
                .memberCount(memberCount)
                .createdAt(r.getCreatedAt().toEpochMilli())
                .joined(joined)
                .postId(postId)
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
