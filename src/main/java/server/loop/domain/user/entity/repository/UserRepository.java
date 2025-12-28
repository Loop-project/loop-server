package server.loop.domain.user.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);// 유저 닉네임 중복 확인

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByNicknameAndDeletedAtIsNull(String nickname);

    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
}