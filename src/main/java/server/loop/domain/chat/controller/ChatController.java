package server.loop.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.chat.dto.req.ChatMessageSendRequest;
import server.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import server.loop.domain.chat.dto.res.ChatMessageResponse;
import server.loop.domain.chat.dto.res.ChatRoomResponse;
import server.loop.domain.chat.service.ChatService;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // ===== STOMP =====
    // 클라 전송: /app/chat.send
    @MessageMapping("/chat.send")
    public void send(@Valid ChatMessageSendRequest req, Principal principal) {
        chatService.sendMessage(principal, req);
    }

    // ===== REST =====
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody @Valid ChatRoomCreateRequest req,
                                                       Principal principal) {
        return ResponseEntity.ok(chatService.createRoom(principal, req));
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<Void> join(@PathVariable String roomId, Principal principal) {
        chatService.joinPublicRoom(principal, roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leave(@PathVariable String roomId, Principal principal) {
        chatService.leaveRoom(principal, roomId);
        return ResponseEntity.ok().build();
    }

    // 공개방 목록 페이징
    @GetMapping("/rooms/public")
    public ResponseEntity<Page<ChatRoomResponse>> listPublicRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(chatService.listPublicRooms(principal, page, size));
    }

    // 내가 속한 방
    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoomResponse>> myRooms(Principal principal) {
        return ResponseEntity.ok(chatService.listMyRooms(principal));
    }

    // 메시지 목록 (무한 스크롤)
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> messages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long beforeId,
            Principal principal) {
        return ResponseEntity.ok(chatService.getMessages(principal, roomId, size, beforeId));
    }
}