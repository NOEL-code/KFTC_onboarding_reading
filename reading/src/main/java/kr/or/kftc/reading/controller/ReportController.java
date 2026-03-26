package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.entity.BookReport;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "사용자 - 독후감", description = "독후감 제출/조회/수정/삭제 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "독후감 제출", description = "HWP 파일과 함께 독후감을 제출합니다.")
    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> submitReport(
            @RequestParam Long courseId,
            @RequestParam String employeeNo,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        ReportResponse response = reportService.submitReport(courseId, employeeNo, title, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "과정별 독후감 목록", description = "특정 독서과정의 독후감 목록을 페이징 조회합니다.")
    @GetMapping("/courses/{courseId}/reports")
    public ResponseEntity<ReportListResponse> getReportsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getReportsByCourse(courseId, page, size));
    }

    @Operation(summary = "독후감 상세 조회", description = "독후감 ID로 상세 정보를 조회합니다.")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportDetailResponse> getReportDetail(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReportDetail(reportId));
    }

    @Operation(summary = "독후감 검색", description = "사번 또는 이름으로 독후감을 검색합니다.")
    @GetMapping("/reports/search")
    public ResponseEntity<ReportSearchResponse> searchReports(
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(reportService.searchReports(employeeNo, name));
    }

    @Operation(summary = "독후감 HWP 다운로드", description = "독후감 파일을 다운로드합니다.")
    @GetMapping("/reports/{reportId}/download")
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

    @Operation(summary = "독후감 수정 (재제출)", description = "보완 요청된 독후감을 수정하여 재제출합니다.")
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long reportId,
            @RequestParam String employeeNo,
            @RequestParam String title,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(reportService.updateReport(reportId, employeeNo, title, file));
    }

    @Operation(summary = "독후감 삭제", description = "본인이 제출한 독후감을 삭제합니다.")
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<MessageResponse> deleteReport(
            @PathVariable Long reportId,
            @RequestParam String employeeNo) {
        reportService.deleteReport(reportId, employeeNo);
        return ResponseEntity.ok(new MessageResponse("독후감이 삭제되었습니다."));
    }
}
