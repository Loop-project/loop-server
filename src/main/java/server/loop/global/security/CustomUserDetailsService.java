package server.loop.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import server.loop.domain.user.entity.User;
import server.loop.domain.user.entity.UserStatus;
import server.loop.domain.user.entity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(this::validateActiveUser)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
    }

    private User validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("활성 상태의 유저가 아닙니다.");
        }
        return user;
    }

    private UserDetails createUserDetails(User user) {
        String password = user.getPassword() != null ? user.getPassword() : ""; //google 로그인 때문에 넣음

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .roles(user.getRole().name())
                .build();
    }
}
