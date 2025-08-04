package server.loop.domain.ad.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.loop.domain.ad.entity.Ad;
import server.loop.domain.ad.service.AdService;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @GetMapping
    public ResponseEntity<List<Ad>> getAds() {
        return ResponseEntity.ok(adService.getActiveAds());
    }

    @PostMapping
    public ResponseEntity<Ad> createAd(@RequestBody Ad ad) {
        Ad savedAd = adService.saveAd(ad);
        return ResponseEntity.ok(savedAd);
    }
}