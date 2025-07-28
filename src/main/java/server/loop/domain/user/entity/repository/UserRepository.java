package server.loop.domain.user.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
}