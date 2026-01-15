package server.loop.domain.post.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import server.loop.domain.notification.service.NotificationService;
import server.loop.domain.post.dto.comment.req.CommentCreateRequestDto;
import server.loop.domain.notification.entity.repository.NotificationRepository;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.*;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest
public class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostReportRepository postReportRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @MockitoBean
    private NotificationService notificationService;

    @AfterEach
    void tearDown() {
        // 외래 키 제약 조건 때문에 삭제 순서가 매우 중요합니다.
        // 자식 테이블 데이터부터 삭제해야 합니다.
        try {
            notificationRepository.deleteAllInBatch();
            postLikeRepository.deleteAllInBatch();
            postReportRepository.deleteAllInBatch();
            postImageRepository.deleteAllInBatch();
            commentRepository.deleteAllInBatch();
            postRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
        } catch (Exception e) {
            System.out.println("TearDown failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("댓글 작성 시 비동기로 알림 전송 메서드가 호출된다")
    void createComment_ShouldTriggerAsyncNotification() {
        // Given
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        User author = User.builder()
                .email("commenter" + uniqueSuffix + "@test.com")
                .password("password")
                .nickname("commenter" + uniqueSuffix)
                .termsOfServiceAgreedAt(java.time.LocalDateTime.now())
                .privacyPolicyAgreedAt(java.time.LocalDateTime.now())
                .build();
        userRepository.save(author);

        User postOwner = User.builder()
                .email("owner" + uniqueSuffix + "@test.com")
                .password("password")
                .nickname("owner" + uniqueSuffix)
                .termsOfServiceAgreedAt(java.time.LocalDateTime.now())
                .privacyPolicyAgreedAt(java.time.LocalDateTime.now())
                .build();
        userRepository.save(postOwner);

        Post post = Post.builder()
                .author(postOwner)
                .category(Category.FREE)
                .title("Test Post")
                .content("Content")
                .build();
        postRepository.save(post);

        CommentCreateRequestDto requestDto = new CommentCreateRequestDto();
        requestDto.setPostId(post.getId());
        requestDto.setContent("Nice post!");

        // When
        commentService.createComment(requestDto, author.getEmail());

        // Then
        await()
            .atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> verify(notificationService, times(1)).send(
                    any(User.class), // sender
                    any(User.class), // receiver
                    any(Post.class),
                    any(server.loop.domain.post.entity.Comment.class),
                    anyString(),
                    anyString()
            ));
    }
}
