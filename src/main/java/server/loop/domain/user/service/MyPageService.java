package server.loop.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.CommentRepository;
import server.loop.domain.post.entity.repository.PostLikeRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.dto.res.MyPagePostResponseDto;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    // 1. 내가 쓴 게시글 조회
    public SliceResponseDto<MyPagePostResponseDto> getMyPosts(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        Slice<Post> myPosts = postRepository.findActivePostsByAuthor(user, pageable);
        return createPostSliceResponse(myPosts);
    }

    // 2. 내가 댓글 단 글 조회
    public SliceResponseDto<MyPagePostResponseDto> getMyCommentedPosts(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        Slice<Post> commentedPosts = commentRepository.findActivePostsCommentedByUser(user, pageable);
        return createPostSliceResponse(commentedPosts);
    }

    // 3. 내가 좋아요 한 글 조회
    public SliceResponseDto<MyPagePostResponseDto> getMyLikedPosts(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        Slice<Post> likedPosts = postLikeRepository.findActivePostsLikedByUser(user, pageable);
        return createPostSliceResponse(likedPosts);
    }

    // 공통 로직: 사용자 이메일로 User 객체 찾기
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 공통 로직: Slice<Post>를 SliceResponseDto<MyPagePostResponseDto>로 변환
    private SliceResponseDto<MyPagePostResponseDto> createPostSliceResponse(Slice<Post> postSlice) {
        Slice<MyPagePostResponseDto> responseDtoSlice = postSlice.map(MyPagePostResponseDto::new);
        return new SliceResponseDto<>(responseDtoSlice);
    }
}