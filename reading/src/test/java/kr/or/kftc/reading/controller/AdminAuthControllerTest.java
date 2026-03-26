package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.LoginRequest;
import kr.or.kftc.reading.dto.LoginResponse;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.AdminAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminAuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AdminAuthService adminAuthService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/admin/login - 로그인 성공")
    void loginSuccess() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("jwt-token").name("관리자1").expiresIn(3600).build();

        when(adminAuthService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"admin01\",\"password\":\"admin1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.name").value("관리자1"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("POST /api/admin/login - 로그인 실패")
    void loginFail() throws Exception {
        when(adminAuthService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"wrong\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }
}
