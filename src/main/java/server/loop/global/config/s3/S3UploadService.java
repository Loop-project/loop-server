package server.loop.global.config.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        // 1. 고유한 파일명 생성
        String originalFileName = multipartFile.getOriginalFilename();
        String uniqueFileName = dirName + "/" + UUID.randomUUID().toString() + "_" + originalFileName;

        // 2. 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // 3. S3 버킷에 파일 업로드
        amazonS3Client.putObject(bucket, uniqueFileName, multipartFile.getInputStream(), metadata);

        // 4. 업로드된 파일의 S3 URL 주소 반환
        return amazonS3Client.getUrl(bucket, uniqueFileName).toString();
    }
}