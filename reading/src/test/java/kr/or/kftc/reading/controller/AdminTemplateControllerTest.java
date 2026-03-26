package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.TemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTemplateController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminTemplateControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TemplateService templateService;
    @MockitoBean private JwtUtil jwtUtil;

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/admin/templates - 양식 업로드 성공")
    void uploadTemplate() throws Exception {
        when(templateService.uploadTemplate(any(), eq(1L)))
                .thenReturn(Map.of(
                        "templateId", 1L,
                        "fileName", "양식.hwp",
                        "fileSize", 1024L,
                        "status", "사용중",
                        "uploadedAt", "2026-03-01T10:00:00"
                ));

        MockMultipartFile file = new MockMultipartFile("file", "양식.hwp", "application/hwp", "data".getBytes());

        mockMvc.perform(multipart("/api/admin/templates")
                        .file(file)
                        .with(authentication(adminAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateId").value(1))
                .andExpect(jsonPath("$.status").value("사용중"));
    }

    @Test
    @DisplayName("DELETE /api/admin/templates/{templateId} - 양식 삭제")
    void deleteTemplate() throws Exception {
        mockMvc.perform(delete("/api/admin/templates/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("양식이 삭제되었습니다."));
    }
}
