package server.loop.global.config.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
public class S3UploadService {

    // 1. AWS SDK v1의 AmazonS3Client 대신 v2의 S3Client를 주입받습니다.
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public String uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty() || Objects.isNull(multipartFile.getOriginalFilename())) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        validateImageFileExtension(originalFileName);

        String uniqueFileName = createUniqueFileName(originalFileName, dirName);

        // 2. v2 SDK에 맞는 PutObjectRequest 객체를 생성합니다.
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();

        // 3. RequestBody를 통해 파일을 업로드합니다.
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

        // 4. 업로드된 파일의 URL을 직접 구성하여 반환합니다.
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, uniqueFileName);
    }

    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            // 5. v2 SDK에 맞는 DeleteObjectRequest 객체를 생성합니다.
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("S3 이미지 삭제 중 오류가 발생했습니다.", e);
        }
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