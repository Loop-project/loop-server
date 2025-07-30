package server.loop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermsType type; // 약관 종류

    @Lob // 아주 긴 텍스트를 저장하기 위한 어노테이션
    @Column(nullable = false)
    private String content; // 약관 내용

    @Column(nullable = false)
    private String version; // 약관 버전 (예: "1.0")

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성(게시) 일시
}