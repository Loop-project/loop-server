package server.loop.domain.user.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.loop.domain.user.dto.res.TermsResponseDto;
import server.loop.domain.user.entity.repository.TermsRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    public TermsResponseDto getLatestTerms(TermsType type) {
        return termsRepository.findFirstByTypeOrderByCreatedAtDesc(type)
                .map(TermsResponseDto::new)
                .orElseThrow(() -> new IllegalArgumentException(type + " 타입의 약관을 찾을 수 없습니다."));
    }
}