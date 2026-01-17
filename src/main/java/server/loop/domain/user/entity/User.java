package server.loop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.global.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime suspendedUntil;
    private String suspendedReason;

    public void grantAdmin() {
        this.role = Role.ADMIN;
    }

    @Column(nullable = false)
    private LocalDateTime termsOfServiceAgreedAt;

    @Column(nullable = false)
    private LocalDateTime privacyPolicyAgreedAt;

    private LocalDateTime marketingConsentAgreedAt;

    // -----------------------

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }


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

    // === 연관관계 편의 메서드 ===
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setAuthor(this);
    }
    //닉네임 수정
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // === 도메인 로직 ===
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }
    // 탈퇴 처리
    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
        this.email = null;
        this.password = null;
        this.nickname = "탈퇴한 회원(" + this.id + ")";
        this.softDelete();
    }

    public void suspend(int days, String reason) {
        this.status = UserStatus.SUSPENDED;
        this.suspendedUntil = LocalDateTime.now().plusDays(days);
        this.suspendedReason = reason;
    }

    public void unsuspend() {
        this.status = UserStatus.ACTIVE;
        this.suspendedUntil = null;
        this.suspendedReason = null;
    }

    public void ban(String reason) {
        this.status = UserStatus.BANNED;
        this.suspendedUntil = null;
        this.suspendedReason = reason;
    }
}
