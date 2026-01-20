package server.loop.domain.post.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService; // S3 서비스 주입
    private final PostImageRepository postImageRepository; // PostImage 리포지토리 주입

    // 게시글 생성
    @Transactional
    public Long createPostInTransaction(PostCreateRequestDto requestDto, List<String> imageUrls, String email) {        log.info("[CreatePost] title={}, category={}, user={}", requestDto.getTitle(), requestDto.getCategory(), email);
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 1. 게시글의 텍스트 내용(제목, 본문 등)을 먼저 DB에 저장합니다.
        Post post = Post.builder()
                .author(author)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .build();

        // 이미지 URL 매핑 (단순 DB 작업)
        if (imageUrls != null) {
            for (String url : imageUrls) {
                post.addImage(new PostImage(url));
            }
        }

        postRepository.save(post);
        log.info("[CreatePost] Success. postId={}", post.getId());
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
    @Transactional
    public List<String> updatePostInTransaction(Long postId, PostUpdateRequestDto requestDto,
                                                List<String> newImageUrls, List<Long> deleteImageIds, String email) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 권한 체크 (간소화)
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        // 텍스트 수정
        post.update(requestDto.getCategory(), requestDto.getTitle(), requestDto.getContent());

        List<String> deletedUrls = new ArrayList<>();

        // 1. 이미지 삭제 처리 (DB 반영)
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            List<PostImage> imagesToDelete = post.getImages().stream()
                    .filter(img -> deleteImageIds.contains(img.getId()))
                    .toList();

            // 삭제될 URL 수집 (반환용)
            deletedUrls.addAll(imagesToDelete.stream().map(PostImage::getImageUrl).toList());

            // 연관관계 끊기 (OrphanRemoval 동작)
            post.getImages().removeAll(imagesToDelete);
        }

        // 2. 새 이미지 추가 (DB 반영)
        if (newImageUrls != null) {
            for (String url : newImageUrls) {
                post.addImage(new PostImage(url));
            }
        }

        // S3에서 지워야 할 URL 목록 반환
        return deletedUrls;
    }



    // 게시글 삭제
    @Transactional
    public List<String> deletePostInTransaction(Long postId, String email) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        // 삭제 전 이미지 URL 백업
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        post.softDelete(); // DB Soft Delete

        return imageUrls; // S3 삭제를 위해 반환
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