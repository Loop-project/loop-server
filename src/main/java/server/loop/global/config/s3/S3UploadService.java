package server.loop.global.config.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Retryable(
            retryFor = {IOException.class, SdkClientException.class},
            exclude = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public String uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty() || Objects.isNull(multipartFile.getOriginalFilename())) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        validateImageFileExtension(originalFileName);
        String uniqueFileName = createUniqueFileName(originalFileName, dirName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, uniqueFileName);

    }

    @Retryable(
            retryFor = {SdkClientException.class},
            exclude = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
    // 업로드 최종 실패 시
    @Recover
    public String recoverUpload(Exception e, MultipartFile file, String dirName) {
        log.error("[S3 Upload Fail] 모든 재시도 실패. file={}, error={}", file.getOriginalFilename(), e.getMessage());
        throw new RuntimeException("S3 업로드에 최종 실패했습니다.", e);
    }

    // 삭제 최종 실패 시
    @Recover
    public void recoverDelete(Exception e, String imageAddress) {
        log.error("[S3 Delete Fail] 모든 재시도 실패. 고아 파일 발생 가능성 있음. url={}, error={}", imageAddress, e.getMessage());
        throw new RuntimeException("S3 이미지 삭제에 최종 실패했습니다.", e);
    }

    // --- 내부 헬퍼 메소드 (변경 없음) ---
    private String createUniqueFileName(String originalFileName, String dirName) {
        return dirName + "/" + UUID.randomUUID().toString() + "_" + originalFileName;
    }

    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("파일에 확장자가 없습니다.");
        }
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif");
        if (!allowedExtensionList.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + extension);
        }
    }

    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new RuntimeException("S3 URL에서 키를 추출하는 중 오류가 발생했습니다.", e);
        }
    }
}