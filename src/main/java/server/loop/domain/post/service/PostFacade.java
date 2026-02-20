package server.loop.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.loop.domain.post.dto.post.req.PostCreateRequestDto;
import server.loop.domain.post.dto.post.req.PostUpdateRequestDto;
import server.loop.global.common.error.ErrorCode;
import server.loop.global.common.exception.CustomException;
import server.loop.global.config.s3.S3UploadService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostFacade {
    private final PostService postService;
    private final S3UploadService s3UploadService;

    // 게시글 생성 Facade
    public Long createPost(PostCreateRequestDto requestDto, List<MultipartFile> images, String email) {
        List<String> uploadedUrls = new ArrayList<>();
        try {
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    uploadedUrls.add(s3UploadService.uploadFile(image, "post-images"));
                }
            }
            return postService.createPostInTransaction(requestDto, uploadedUrls, email);
        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패. error={}", e.getMessage());
            deleteUploadedImages(uploadedUrls);
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD, e.getMessage());
        } catch (Exception e) {
            log.error("게시글 생성 실패. 업로드된 이미지 롤백 수행. error={}", e.getMessage());
            deleteUploadedImages(uploadedUrls);
            throw e;
        }
    }

    // 2. 게시글 수정 Facade
    public Long updatePost(Long postId, PostUpdateRequestDto requestDto, List<MultipartFile> newImages, String email) {

        List<String> newUploadedUrls = new ArrayList<>();

        try {
            // 1) 새 이미지 S3 업로드
            if (newImages != null && !newImages.isEmpty()) {
                for (MultipartFile image : newImages) {
                    newUploadedUrls.add(s3UploadService.uploadFile(image, "post-images"));
                }
            }

            // 2) DB 업데이트 진행
            // requestDto.getDeleteImageIds()로 DTO에서 꺼내서 넘깁니다.
            List<String> urlsToDelete = postService.updatePostInTransaction(
                    postId,
                    requestDto,
                    newUploadedUrls,
                    requestDto.getDeleteImageIds(), // [여기 수정됨] DTO에서 꺼냄
                    email
            );

            // 3) 구 이미지 S3 삭제
            deleteUploadedImages(urlsToDelete);

            return postId;

        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패. error={}", e.getMessage());
            deleteUploadedImages(newUploadedUrls);
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD, e.getMessage());
        } catch (Exception e) {
            log.error("게시글 수정 실패. 업로드된 새 이미지 롤백 수행.");
            deleteUploadedImages(newUploadedUrls);
            throw e;
        }
    }

    // 3. 게시글 삭제 Facade
    public void deletePost(Long postId, String email) {
        // DB에서 먼저 삭제하고, 삭제된 이미지 URL 목록을 받아옴
        List<String> urlsToDelete = postService.deletePostInTransaction(postId, email);

        // S3 파일 삭제
        deleteUploadedImages(urlsToDelete);
    }

    private void deleteUploadedImages(List<String> urls) {
        if (urls == null) return;
        for (String url : urls) {
            try {
                s3UploadService.deleteImageFromS3(url);
            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패 (고아 객체 발생 가능성): {}", url, e);
            }
        }
    }
}
