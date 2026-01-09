package server.loop.domain.ad.entity.repository;

import server.loop.domain.ad.entity.Ad;

import java.time.LocalDate;
import java.util.List;

public interface AdRepositoryCustom {
    // 특정 날짜에 노출 가능한 활성 광고 조회
    List<Ad> findActiveAds(LocalDate targetDate);
}
