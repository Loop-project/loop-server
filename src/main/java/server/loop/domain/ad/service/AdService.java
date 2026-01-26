package server.loop.domain.ad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import server.loop.domain.ad.entity.Ad;
import server.loop.domain.ad.entity.repository.AdRepository;
import server.loop.global.common.error.ErrorCode;
import server.loop.global.common.exception.CustomException;
import server.loop.global.config.s3.S3UploadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdService {

    private final AdRepository adRepository;
    private final S3UploadService s3UploadService;

    public List<Ad> getActiveAds() {
        LocalDate today = LocalDate.now();
        return adRepository.findActiveAds(today);
    }
    public Ad createAd(MultipartFile image, String linkUrl, LocalDate startDate, LocalDate endDate, boolean active) {
        // 1. S3 업로드
        String imageUrl;
        try {
            imageUrl = s3UploadService.uploadFile(image, "ads");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD, e.getMessage());
        }

        // 2. 광고 엔티티 생성 및 저장
        Ad ad = Ad.builder()
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .startDate(startDate)
                .endDate(endDate)
                .active(active)
                .build();

        return adRepository.save(ad);
    }
}