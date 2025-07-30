package server.loop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Where(clause = "deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // --- 약관 동의 필드 추가 ---
    @Column(nullable = false)
    private LocalDateTime termsOfServiceAgreedAt; // 이용약관 동의 일시

    @Column(nullable = false)
    private LocalDateTime privacyPolicyAgreedAt; // 개인정보 처리방침 동의 일시

    private LocalDateTime marketingConsentAgreedAt; // (선택) 마케팅 정보 수신 동의 일시
    // -----------------------

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public User(String email, String password, String nickname, Integer age, Gender gender,
                LocalDateTime termsOfServiceAgreedAt, LocalDateTime privacyPolicyAgreedAt, LocalDateTime marketingConsentAgreedAt) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.termsOfServiceAgreedAt = termsOfServiceAgreedAt;
        this.privacyPolicyAgreedAt = privacyPolicyAgreedAt;
        this.marketingConsentAgreedAt = marketingConsentAgreedAt;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    // 탈퇴 처리
    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
        this.email = null; // 이메일 초기화 (재가입 가능)
        this.password = null; // 비밀번호 초기화
        this.nickname = "탈퇴한 회원입니다." + this.id;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
