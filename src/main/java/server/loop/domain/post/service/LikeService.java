package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.dto.post.res.PostLikeResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.TopLikedPostResponseDto;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostLike;
import server.loop.domain.post.entity.repository.PostLikeRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostLikeResponseDto toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // Optional을 활용하여 좋아요 존재 여부 확인
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀다면 -> 좋아요 취소
            postLikeRepository.delete(existingLike.get());
        } else {
            // 좋아요를 누르지 않았다면 -> 좋아요 추가
            PostLike like = new PostLike(user);
            like.setPost(post);
            postLikeRepository.save(like);
        }

        // 최신 좋아요 상태를 다시 확인
        boolean likedByUser = postLikeRepository.findByUserAndPost(user, post).isPresent();
        // 최신 좋아요 수
        long likeCount = postLikeRepository.countByPost(post); // long 타입으로 받기

        // DTO에 int 타입이 필요하다면 캐스팅
        int likeCountAsInt = (int) likeCount;

        return new PostLikeResponseDto(likedByUser, likeCountAsInt);
    }


    public List<TopLikedPostResponseDto> getYesterdayTop5LikedPosts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(LocalTime.MAX);

        System.out.println("쿼리 범위: " + start + " ~ " + end);

        List<Post> posts = postLikeRepository.findTopPostsCreatedInPeriodOrderByLikesNative(start, end);

        return posts.stream()
                .map(TopLikedPostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

}