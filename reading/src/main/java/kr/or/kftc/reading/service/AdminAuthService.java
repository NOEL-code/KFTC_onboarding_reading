package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.LoginRequest;
import kr.or.kftc.reading.dto.LoginResponse;
import kr.or.kftc.reading.entity.Admin;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.AdminRepository;
import kr.or.kftc.reading.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getLoginId(), admin.getName());

        return LoginResponse.builder()
                .token(token)
                .name(admin.getName())
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
    }
}
