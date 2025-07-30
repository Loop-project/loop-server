package server.loop.domain.user.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.user.entity.Terms;
import server.loop.domain.user.entity.TermsType;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    // 특정 타입의 약관 중 가장 최신 버전(가장 최근에 생성된) 1개를 찾아오는 메소드
    Optional<Terms> findFirstByTypeOrderByCreatedAtDesc(TermsType type);
}