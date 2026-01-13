package server.loop.domain.ad.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.loop.domain.ad.entity.Ad;
import server.loop.domain.ad.service.AdService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @GetMapping
    public ResponseEntity<List<Ad>> getAds() {
        return ResponseEntity.ok(adService.getActiveAds());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Ad> createAd(
            @RequestPart("image") MultipartFile image,
            @RequestPart("linkUrl") String linkUrl,
            @RequestPart("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestPart("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestPart("active") boolean active
    ) throws IOException {
        log.info("[CreateAd] linkUrl={}, start={}, end={}, active={}", linkUrl, startDate, endDate, active);
        Ad savedAd = adService.createAd(image, linkUrl, startDate, endDate, active);
        return ResponseEntity.ok(savedAd);
    }

}