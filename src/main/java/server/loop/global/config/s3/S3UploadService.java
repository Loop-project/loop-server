package server.loop.global.config.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 1. 파일 업로드 (스트림 방식 + 유효성 검사)
    public String uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty() || Objects.isNull(multipartFile.getOriginalFilename())) {
            // throw new S3Exception(ErrorCode.EMPTY_FILE_EXCEPTION); // 직접 만드신 예외 사용
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        validateImageFileExtension(originalFileName); // 확장자 검사

        String uniqueFileName = createUniqueFileName(originalFileName, dirName);

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // 스트림을 통해 S3에 직접 업로드 (메모리 효율적)
        amazonS3Client.putObject(bucket, uniqueFileName, multipartFile.getInputStream(), metadata);

        return amazonS3Client.getUrl(bucket, uniqueFileName).toString();
    }

    // 2. 파일 삭제
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            amazonS3Client.deleteObject(bucket, key);
        } catch (Exception e) {
            // throw new S3Exception(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
            throw new RuntimeException("S3 이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }

    // --- 내부 헬퍼 메소드 ---

    // 고유한 파일명 생성
    private String createUniqueFileName(String originalFileName, String dirName) {
        return dirName + "/" + UUID.randomUUID().toString() + "_" + originalFileName;
    }

    // 파일 확장자 유효성 검사
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            // throw new S3Exception(ErrorCode.NO_FILE_EXTENSION);
            throw new IllegalArgumentException("파일에 확장자가 없습니다.");
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtensionList.contains(extension)) {
            // throw new S3Exception(ErrorCode.INVALID_FILE_EXTENSION);
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + extension);
        }
    }

    // 이미지 주소에서 S3 객체 키 추출
    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            // throw new S3Exception(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
            throw new RuntimeException("S3 URL에서 키를 추출하는 중 오류가 발생했습니다.", e);
        }
    }
}