package server.loop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.chat.dto.res.ChatRoomResponse;
import server.loop.domain.chat.entity.repository.ChatRoomRepository;
import server.loop.domain.chat.service.ChatService;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 끝나면 DB 롤백됨
class ChatServiceTest {

    @Autowired ChatService chatService;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("중고거래 게시글에서 채팅을 시작하면 방이 생성되고, 다시 누르면 기존 방을 반환한다.")
    void startChatTest() {
        // 1. Given (데이터 준비)
        User seller = userRepository.save(User.builder()
                .email("seller@test.com")
                .nickname("판매자")
                .termsOfServiceAgreedAt(java.time.LocalDateTime.now())
                .privacyPolicyAgreedAt(java.time.LocalDateTime.now())
                .build());
        User buyer = userRepository.save(User.builder()
                .email("buyer@test.com")
                .nickname("구매자")
                .termsOfServiceAgreedAt(java.time.LocalDateTime.now())
                .privacyPolicyAgreedAt(java.time.LocalDateTime.now())
                .build());

        Post usedPost = postRepository.save(Post.builder()
                .author(seller)
                .title("중고 물품")
                .content("팝니다")
                .category(Category.USED) // 중요: USED 카테고리
                .build());

        // 로그인한 사용자(Buyer) 흉내내기
        org.springframework.security.core.userdetails.UserDetails buyerDetails = org.springframework.security.core.userdetails.User.builder()
                .username(buyer.getEmail())
                .password("password")
                .roles("USER")
                .build();

        // 2. When (첫 번째 클릭 - 방 생성)
        ChatRoomResponse room1 = chatService.startPrivateChat(buyerDetails, usedPost.getId());

        // 3. Then (검증)
        assertThat(room1).isNotNull();
        assertThat(room1.getTitle()).contains("중고 물품");
        assertThat(chatRoomRepository.count()).isEqualTo(1); // 방이 1개여야 함

        // 4. When (두 번째 클릭 - 재입장)
        ChatRoomResponse room2 = chatService.startPrivateChat(buyerDetails, usedPost.getId());

        // 5. Then (검증 - 방이 늘어나지 않고 ID가 같아야 함)
        assertThat(chatRoomRepository.count()).isEqualTo(1); // 여전히 1개여야 함
        assertThat(room1.getRoomId()).isEqualTo(room2.getRoomId()); // ID가 같아야 함

        System.out.println("첫번째 방 ID: " + room1.getRoomId());
        System.out.println("두번째 방 ID: " + room2.getRoomId());
    }
}