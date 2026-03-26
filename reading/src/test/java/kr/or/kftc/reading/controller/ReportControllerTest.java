package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReportService reportService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/reports - 독후감 제출 성공")
    void submitReport() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).status("제출").submittedAt(LocalDateTime.now()).build();

        when(reportService.submitReport(eq(1L), eq("20210001"), eq("독후감1"), any()))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "report.hwp", "application/hwp", "data".getBytes());

        mockMvc.perform(multipart("/api/reports")
                        .file(file)
                        .param("courseId", "1")
                        .param("employeeNo", "20210001")
                        .param("title", "독후감1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value(1))
                .andExpect(jsonPath("$.status").value("제출"));
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/reports - 과정별 독후감 목록")
    void getReportsByCourse() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .totalCount(1).page(1).size(10).totalPages(1)
                .reports(List.of(
                        ReportListResponse.ReportSummary.builder()
                                .reportId(1L).name("김민수").department("IT개발부")
                                .title("독후감1").status("제출").submittedAt("2026-03-01")
                                .build()))
                .build();

        when(reportService.getReportsByCourse(1L, 1, 10)).thenReturn(response);

        mockMvc.perform(get("/api/courses/1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.reports[0].name").value("김민수"));
    }

    @Test
    @DisplayName("GET /api/reports/{reportId} - 독후감 상세 조회")
    void getReportDetail() throws Exception {
        ReportDetailResponse response = ReportDetailResponse.builder()
                .reportId(1L).courseName("26년 상반기").employeeNo("20210001")
                .name("김민수").department("IT개발부").title("독후감1")
                .fileName("report.hwp").fileSize(1024L)
                .status("제출").submittedAt(LocalDateTime.now())
                .build();

        when(reportService.getReportDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(1))
                .andExpect(jsonPath("$.courseName").value("26년 상반기"))
                .andExpect(jsonPath("$.name").value("김민수"));
    }

    @Test
    @DisplayName("GET /api/reports/{reportId} - 존재하지 않는 독후감")
    void getReportDetailNotFound() throws Exception {
        when(reportService.getReportDetail(999L))
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/reports/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("독후감을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("GET /api/reports/search - 독후감 검색")
    void searchReports() throws Exception {
        ReportSearchResponse response = ReportSearchResponse.builder()
                .results(List.of(
                        ReportSearchResponse.SearchResult.builder()
                                .reportId(1L).courseName("26년 상반기").name("김민수")
                                .department("IT개발부").title("독후감1")
                                .status("제출").submittedAt("2026-03-01")
                                .build()))
                .build();

        when(reportService.searchReports("20210001", null)).thenReturn(response);

        mockMvc.perform(get("/api/reports/search").param("employeeNo", "20210001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("김민수"));
    }

    @Test
    @DisplayName("PUT /api/reports/{reportId} - 독후감 수정")
    void updateReport() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(1L).status("제출").updatedAt(LocalDateTime.now()).build();

        when(reportService.updateReport(eq(1L), eq("20210001"), eq("수정된 제목"), any()))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "new.hwp", "application/hwp", "data".getBytes());

        mockMvc.perform(multipart("/api/reports/1")
                        .file(file)
                        .param("employeeNo", "20210001")
                        .param("title", "수정된 제목")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(1));
    }

    @Test
    @DisplayName("DELETE /api/reports/{reportId} - 독후감 삭제")
    void deleteReport() throws Exception {
        mockMvc.perform(delete("/api/reports/1").param("employeeNo", "20210001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("독후감이 삭제되었습니다."));
    }
}
