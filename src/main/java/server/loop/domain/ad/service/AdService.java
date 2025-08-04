package server.loop.domain.ad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.ad.entity.Ad;
import server.loop.domain.ad.entity.repository.AdRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdService {

    private final AdRepository adRepository;

    public List<Ad> getActiveAds() {
        LocalDate today = LocalDate.now();
        return adRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }
    @Transactional
    public Ad saveAd(Ad ad) {
        return adRepository.save(ad);
    }
}