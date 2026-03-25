package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.AdminReportListResponse;
import kr.or.kftc.reading.dto.ReportIdsRequest;
import kr.or.kftc.reading.entity.BookReport;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    // API 19: GET /api/admin/reports — 관리자 독후감 목록
    @GetMapping
    public ResponseEntity<AdminReportListResponse> getAdminReports(
            @RequestParam Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getAdminReports(courseId, status, department, name, page, size));
    }

    // API 20: GET /api/admin/reports/{reportId}/download — 독후감 hwp 다운로드
    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId) {
        BookReport report = reportService.getReportEntity(reportId);

        if (report.getFileData() == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "미제출 상태의 독후감은 다운로드할 수 없습니다.");
        }

        String encodedFileName = URLEncoder.encode(report.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(report.getFileData());
    }

    // API 21: PATCH /api/admin/reports/approve — 일괄 승인
    @PatchMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveReports(@RequestBody ReportIdsRequest request) {
        return ResponseEntity.ok(reportService.approveReports(request.getReportIds()));
    }

    // API 22: PATCH /api/admin/reports/supplement — 일괄 보완
    @PatchMapping("/supplement")
    public ResponseEntity<Map<String, Object>> supplementReports(@RequestBody ReportIdsRequest request) {
        return ResponseEntity.ok(reportService.supplementReports(request.getReportIds()));
    }
}
