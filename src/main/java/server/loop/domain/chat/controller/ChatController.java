package server.loop.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.chat.dto.req.ChatMessageSendRequest;
import server.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import server.loop.domain.chat.dto.res.ChatMessageResponse;
import server.loop.domain.chat.dto.res.ChatRoomResponse;
import server.loop.domain.chat.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // ===== STOMP =====
    // 클라 전송: /app/chat.send
    // spring-security-messaging 사용 시 @AuthenticationPrincipal 주입 가능
    @MessageMapping("/chat.send")
    public void send(@Valid ChatMessageSendRequest req,
                     @AuthenticationPrincipal UserDetails userDetails) {
        chatService.sendMessage(userDetails, req);
    }

    // ===== REST =====

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody @Valid ChatRoomCreateRequest req,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.createRoom(userDetails, req));
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<Void> join(@PathVariable String roomId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        chatService.joinPublicRoom(userDetails, roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leave(@PathVariable String roomId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        chatService.leaveRoom(userDetails, roomId);
        return ResponseEntity.ok().build();
    }

    // 공개방 목록: 비회원도 조회 가능 (userDetails == null 허용)
    @GetMapping("/rooms/public")
    public ResponseEntity<Page<ChatRoomResponse>> listPublicRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.listPublicRooms(userDetails, page, size));
    }

    // 내가 속한 방: 인증 필요
    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoomResponse>> myRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.listMyRooms(userDetails));
    }

    // 메시지 목록 (무한 스크롤): 현재 정책상 멤버만 조회 가능
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> messages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long beforeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.getMessages(userDetails, roomId, size, beforeId));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(@PathVariable String roomId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.getRoom(userDetails, roomId));
    }
}
