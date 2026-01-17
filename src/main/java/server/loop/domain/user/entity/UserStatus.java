package server.loop.domain.user.entity;

public enum UserStatus {
    ACTIVE,         // 정상
    SUSPENDED,      // 정지
    BANNED,         // 영구 정지
    WITHDRAWN       // 탈퇴
}
