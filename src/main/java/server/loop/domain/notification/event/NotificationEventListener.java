package server.loop.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import server.loop.domain.notification.service.NotificationService;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.CommentRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.post.event.CommentCreatedEvent;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            log.info("[NotificationEventListener] Processing event for comment id={}", event.getCommentId());

            User sender = userRepository.findById(event.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + event.getSenderId()));
            User receiver = userRepository.findById(event.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + event.getReceiverId()));
            Post post = postRepository.findById(event.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Post not found: " + event.getPostId()));
            Comment comment = commentRepository.findById(event.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + event.getCommentId()));

            notificationService.send(
                    sender,
                    receiver,
                    post,
                    comment,
                    event.getPostTitle(),
                    event.getMessage()
            );
        } catch (Exception e) {
            log.error("[Notification Failed] commentId={}, error={}", 
                    event.getCommentId(), e.getMessage(), e);
        }
    }
}
