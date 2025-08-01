package server.loop.domain.user.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.user.entity.Terms;
import server.loop.domain.user.entity.TermsType;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    Optional<Terms> findFirstByTypeOrderByVersionDesc(TermsType type);
}