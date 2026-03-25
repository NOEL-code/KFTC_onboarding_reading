package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.LoginRequest;
import kr.or.kftc.reading.dto.LoginResponse;
import kr.or.kftc.reading.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    // API 9: POST /api/admin/login — 관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }
}
