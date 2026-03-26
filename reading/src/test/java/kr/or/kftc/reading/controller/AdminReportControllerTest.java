package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.AdminReportListResponse;
import kr.or.kftc.reading.entity.BookReport;
import kr.or.kftc.reading.entity.ReportStatus;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReportController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReportService reportService;
    @MockitoBean private JwtUtil jwtUtil;

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("GET /api/admin/reports - 관리자 독후감 목록")
    void getAdminReports() throws Exception {
        AdminReportListResponse response = AdminReportListResponse.builder()
                .summary(AdminReportListResponse.Summary.builder()
                        .total(10).submitted(5).approved(3).supplement(1).notSubmitted(1).build())
                .page(1).size(10).totalPages(1)
                .reports(List.of(
                        AdminReportListResponse.AdminReportItem.builder()
                                .reportId(1L).enrollmentId(1L).name("김민수")
                                .team("IT개발팀").title("독후감1")
                                .status("제출").submittedAt("2026-03-01")
                                .build()))
                .build();

        when(reportService.getAdminReports(eq(1L), any(), any(), any(), eq(1), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/admin/reports")
                        .param("courseId", "1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").value(10))
                .andExpect(jsonPath("$.reports[0].name").value("김민수"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/{reportId}/download - 독후감 다운로드")
    void downloadReport() throws Exception {
        BookReport report = BookReport.builder()
                .id(1L).fileName("report.hwp").fileData("file-content".getBytes())
                .fileSize(12L).status(ReportStatus.제출).build();

        when(reportService.getReportEntity(1L)).thenReturn(report);

        mockMvc.perform(get("/api/admin/reports/1/download")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes("file-content".getBytes()));
    }

    @Test
    @DisplayName("PATCH /api/admin/reports/approve - 일괄 승인")
    void approveReports() throws Exception {
        when(reportService.approveReports(List.of(1L, 2L)))
                .thenReturn(Map.of("updatedCount", 2, "message", "2건이 승인되었습니다."));

        mockMvc.perform(patch("/api/admin/reports/approve")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(2));
    }

    @Test
    @DisplayName("PATCH /api/admin/reports/supplement - 일괄 보완")
    void supplementReports() throws Exception {
        when(reportService.supplementReports(eq(List.of(1L)), any()))
                .thenReturn(Map.of("updatedCount", 1, "message", "1건이 보완 요청되었습니다."));

        mockMvc.perform(patch("/api/admin/reports/supplement")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportIds\":[1],\"reason\":\"내용 보완 필요\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(1));
    }

    @Test
    @DisplayName("GET /api/admin/reports - 인증 없이 접근하면 403")
    void getAdminReportsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/reports").param("courseId", "1"))
                .andExpect(status().isForbidden());
    }
}
