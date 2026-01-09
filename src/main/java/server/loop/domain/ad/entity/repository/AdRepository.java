package server.loop.domain.ad.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.ad.entity.Ad;

public interface AdRepository extends JpaRepository<Ad, Long>, AdRepositoryCustom {
}