package server.loop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import server.loop.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermsType type; // 약관 종류

    @Lob
    @Column(nullable = false)
    private String content; // 약관 내용

    @Column(nullable = false)
    private String version; // 약관 버전 (예: "1.0")

    @Builder
    public Terms(TermsType type, String content, String version) {
        this.type = type;
        this.content = content;
        this.version = version;
    }

    public void updateContent(String content, String version) {
        this.content = content;
        this.version = version;
    }
}
