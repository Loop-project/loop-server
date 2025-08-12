package server.loop.domain.post.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import server.loop.domain.post.dto.post.req.PostCreateRequestDto;
import server.loop.domain.post.dto.post.req.PostUpdateRequestDto;
import server.loop.domain.post.dto.post.res.PostDetailResponseDto;
import server.loop.domain.post.dto.post.res.PostResponseDto;
import server.loop.domain.post.dto.post.res.SliceResponseDto;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostImage;
import server.loop.domain.post.entity.repository.PostImageRepository;
import server.loop.domain.post.entity.repository.PostLikeRepository;
import server.loop.domain.post.entity.repository.PostRepository;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.repository.UserRepository;
import server.loop.global.config.s3.S3UploadService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final S3UploadService s3UploadService; // S3 서비스 주입
    private final PostImageRepository postImageRepository; // PostImage 리포지토리 주입

    // 게시글 생성
    public Long createPost(PostCreateRequestDto requestDto, List<MultipartFile> images, String email) throws IOException {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 1. 게시글의 텍스트 내용(제목, 본문 등)을 먼저 DB에 저장합니다.
        Post post = Post.builder()
                .author(author)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .build();
        postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3UploadService.uploadFile(image, "post-images");
                PostImage postImage = new PostImage(imageUrl);
                post.addImage(postImage); // 편의 메서드로 Post-Image 관계 세팅
            }
        }

        return post.getId();
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PostDetailResponseDto getPost(Long postId, User currentUser) {
        Post post = postRepository.findActivePostWithCommentsById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        boolean likedByUser = currentUser != null && post.isLikedBy(currentUser);
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
    public Long updatePost(Long postId, PostUpdateRequestDto requestDto, List<MultipartFile> images, String email)
            throws AccessDeniedException, IOException {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 작성자 본인 확인
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        // 게시글 내용 수정
        post.update(requestDto.getCategory(), requestDto.getTitle(), requestDto.getContent());

        // === 이미지 처리 ===
        if (images != null && !images.isEmpty()) {
            // 1) 기존 이미지 S3에서 삭제
            post.getImages().forEach(img -> s3UploadService.deleteImageFromS3(img.getImageUrl()));
            // 2) DB에서 기존 이미지 레코드 제거
            post.getImages().clear();

            // 3) 새 이미지 업로드
            for (MultipartFile image : images) {
                String imageUrl = s3UploadService.uploadFile(image, "post-images");
                PostImage postImage = new PostImage(imageUrl);
                post.addImage(postImage);
            }
        }
        // images가 null 또는 비어있으면 → 기존 이미지 유지

        return postId;
    }


    // 게시글 삭제
    public void deletePost(Long postId, String email) throws AccessDeniedException {
        // 1. 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 로그인한 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. ⭐ 권한 확인: 게시글 작성자와 로그인 사용자가 일치하는지 확인
        if (!post.getAuthor().equals(user)) {
            // 작성자가 아닐 경우 AccessDeniedException 발생
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        post.softDelete();
    }

    @Transactional(readOnly = true)
    public SliceResponseDto<PostResponseDto> searchPosts(String q, Pageable pageable) {
        Slice<Post> postSlice;
        if (q == null || q.trim().isEmpty()) {
            // 키워드 없으면 전체 최신순과 동일하게
            postSlice = postRepository.findAllActivePosts(pageable);
        } else {
            postSlice = postRepository.searchActivePosts(q.trim(), pageable);
        }
        Slice<PostResponseDto> dtoSlice = postSlice.map(PostResponseDto::new);
        return new SliceResponseDto<>(dtoSlice);
    }
}