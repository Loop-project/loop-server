package server.loop.global;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // 임시로 최상위 경로 사용
public class TestController {

    @GetMapping("/test")
    public String test() {
        // 이 메시지가 보이면 최신 코드가 배포된 것입니다.
        return "Deployment Success - v2";
    }
}