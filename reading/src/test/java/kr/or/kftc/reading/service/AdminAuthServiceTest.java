package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.LoginRequest;
import kr.or.kftc.reading.dto.LoginResponse;
import kr.or.kftc.reading.entity.Admin;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.AdminRepository;
import kr.or.kftc.reading.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock private AdminRepository adminRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AdminAuthService adminAuthService;

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {
        Admin admin = Admin.builder().id(1L).loginId("admin01").password("encoded").name("관리자1").build();
        when(adminRepository.findByLoginId("admin01")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "admin01", "관리자1")).thenReturn("jwt-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

        LoginRequest request = new LoginRequest();
        request.setLoginId("admin01");
        request.setPassword("admin1234");

        LoginResponse response = adminAuthService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getName()).isEqualTo("관리자1");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 없음")
    void loginFailIdNotFound() {
        when(adminRepository.findByLoginId("wrong")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setLoginId("wrong");
        request.setPassword("password");

        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFailWrongPassword() {
        Admin admin = Admin.builder().id(1L).loginId("admin01").password("encoded").build();
        when(adminRepository.findByLoginId("admin01")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setLoginId("admin01");
        request.setPassword("wrong");

        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");
    }
}
