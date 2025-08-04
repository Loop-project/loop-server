package server.loop.domain.ad.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;  // 광고 이미지 URL

    @Column(nullable = false)
    private String linkUrl;   // 클릭 시 이동할 링크

    private LocalDate startDate; // 노출 시작일
    private LocalDate endDate;   // 노출 종료일

    @Column(nullable = false)
    private boolean active;   // 활성화 여부
}