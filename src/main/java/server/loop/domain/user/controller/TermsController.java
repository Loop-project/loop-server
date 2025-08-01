package server.loop.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import server.loop.domain.user.dto.res.TermsResponseDto;
import server.loop.domain.user.entity.TermsType;
import server.loop.domain.user.service.TermsService;

@Tag(name = "Terms", description = "약관 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms")
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "최신 약관 조회", description = "특정 타입의 가장 최신 버전 약관을 조회합니다.")
    @GetMapping
    public ResponseEntity<TermsResponseDto> getLatestTerms(@RequestParam TermsType type) {
        TermsResponseDto response = termsService.getLatestTerms(type);
        return ResponseEntity.ok(response);
    }
}