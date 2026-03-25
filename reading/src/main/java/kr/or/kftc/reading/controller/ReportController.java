package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // API 1: POST /api/reports — 독후감 제출
    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> submitReport(
            @RequestParam Long courseId,
            @RequestParam String employeeNo,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        ReportResponse response = reportService.submitReport(courseId, employeeNo, title, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API 2: GET /api/courses/{courseId}/reports — 과정별 독후감 목록
    @GetMapping("/courses/{courseId}/reports")
    public ResponseEntity<ReportListResponse> getReportsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getReportsByCourse(courseId, page, size));
    }

    // API 3: GET /api/reports/{reportId} — 독후감 상세 조회
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportDetailResponse> getReportDetail(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReportDetail(reportId));
    }

    // API 5: GET /api/reports/search — 독후감 검색
    @GetMapping("/reports/search")
    public ResponseEntity<ReportSearchResponse> searchReports(
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(reportService.searchReports(employeeNo, name));
    }

    // API 6: PUT /api/reports/{reportId} — 독후감 수정 (재제출)
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long reportId,
            @RequestParam String employeeNo,
            @RequestParam(required = false) String title,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(reportService.updateReport(reportId, employeeNo, title, file));
    }

    // API 7: DELETE /api/reports/{reportId} — 독후감 삭제
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<MessageResponse> deleteReport(
            @PathVariable Long reportId,
            @RequestParam String employeeNo) {
        reportService.deleteReport(reportId, employeeNo);
        return ResponseEntity.ok(new MessageResponse("독후감이 삭제되었습니다."));
    }
}
