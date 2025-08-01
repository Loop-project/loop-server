package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.post.entity.repository.PostLikeRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public String toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 이미 좋아요를 눌렀는지 확인
        return postLikeRepository.findByUserAndPost(user, post)
                .map(like -> {
                    // 이미 좋아요를 눌렀다면 -> 좋아요 취소
                    postLikeRepository.delete(like);
                    return "좋아요를 취소했습니다.";
                })
                .orElseGet(() -> {
                    // 좋아요를 누르지 않았다면 -> 좋아요 추가
                    PostLike like = new PostLike(user);
                    like.setPost(post); // 연관관계 편의 메서드 사용
                    postLikeRepository.save(like);
                    return "좋아요를 눌렀습니다.";
                });
    }
}