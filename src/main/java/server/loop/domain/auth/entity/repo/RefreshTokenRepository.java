package server.loop.domain.auth.entity.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.auth.entity.RefreshToken;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
}