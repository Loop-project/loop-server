package server.loop.domain.ad.entity.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import server.loop.domain.ad.entity.Ad;

import java.time.LocalDate;
import java.util.List;

import static server.loop.domain.ad.entity.QAd.ad;

@RequiredArgsConstructor
public class AdRepositoryImpl implements AdRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Ad> findActiveAds(LocalDate targetDate) {
        return queryFactory
                .selectFrom(ad)
                .where(
                        ad.active.isTrue(), // 활성화된 광고만
                        ad.startDate.loe(targetDate), // 시작일 <= 오늘
                        ad.endDate.goe(targetDate)    // 종료일 >= 오늘
                )
                .fetch();
    }
}
