package server.loop.domain.post.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.post.dto.post.req.PostCreateRequestDto;
import server.loop.domain.post.dto.post.req.PostUpdateRequestDto;
import server.loop.domain.post.dto.post.res.PostDetailResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.repository.PostLikeRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    // 게시글 생성
    public Long createPost(PostCreateRequestDto requestDto, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Post post = Post.builder()
                .author(author)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PostDetailResponseDto getPost(Long postId, User currentUser) {
        Post post = postRepository.findActivePostById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        boolean likedByUser = post.isLikedBy(currentUser);
        return new PostDetailResponseDto(post, likedByUser);
    }

    // 게시글 카테고리 별 조회
    @Transactional(readOnly = true)
    public SliceResponseDto<PostResponseDto> getPostsSlice(Category category, Pageable pageable) {
        Slice<Post> postSlice;
        if (category != null) {
            postSlice = postRepository.findAllActivePostsByCategory(category, pageable);
        } else {
            postSlice = postRepository.findAllActivePosts(pageable);
        }

        // Slice<Post>를 Slice<PostResponseDto>로 변환
        Slice<PostResponseDto> responseDtoSlice = postSlice.map(PostResponseDto::new);

        return new SliceResponseDto<>(responseDtoSlice);
    }

    // 게시글 수정
    public Long updatePost(Long postId, PostUpdateRequestDto requestDto, String email) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 작성자 본인인지 확인
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        // Post Entity에 만들어 둔 update 메소드 사용
        post.update(requestDto.getCategory(), requestDto.getTitle(), requestDto.getContent());
        return postId;
    }

    // 게시글 삭제
    public void deletePost(Long postId, String email) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 작성자 본인인지 확인
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}