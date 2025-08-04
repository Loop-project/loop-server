package server.loop.domain.ad.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.ad.entity.Ad;

import java.time.LocalDate;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate today1, LocalDate today2
    );
}
