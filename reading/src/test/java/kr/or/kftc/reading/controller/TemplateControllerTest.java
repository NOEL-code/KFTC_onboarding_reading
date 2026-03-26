package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.entity.ReportTemplate;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.TemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TemplateControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TemplateService templateService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/templates/download - 양식 다운로드 성공")
    void downloadTemplate() throws Exception {
        ReportTemplate template = ReportTemplate.builder()
                .id(1L).fileName("독후감양식.hwp").fileData("test-data".getBytes())
                .fileSize(9L).status("사용중").build();

        when(templateService.getActiveTemplate()).thenReturn(template);

        mockMvc.perform(get("/api/templates/download"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes("test-data".getBytes()));
    }

    @Test
    @DisplayName("GET /api/templates/download - 양식 없음")
    void downloadTemplateNotFound() throws Exception {
        when(templateService.getActiveTemplate())
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "사용 가능한 양식이 없습니다."));

        mockMvc.perform(get("/api/templates/download"))
                .andExpect(status().isNotFound());
    }
}
