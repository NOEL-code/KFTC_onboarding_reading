package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.AdminReportListResponse;
import kr.or.kftc.reading.dto.ReportIdsRequest;
import kr.or.kftc.reading.dto.SupplementRequest;
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

@Tag(name = "관리자 - 독후감 관리", description = "독후감 조회/다운로드/승인/보완 API (JWT 필요)")
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "관리자 독후감 목록", description = "과정별 독후감 목록을 상태/부서/이름 필터와 함께 조회합니다.")
    @GetMapping
    public ResponseEntity<AdminReportListResponse> getAdminReports(
            @RequestParam Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getAdminReports(courseId, status, team, name, page, size));
    }

    @Operation(summary = "독후감 HWP 다운로드", description = "독후감 파일을 다운로드합니다.")
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

    @Operation(summary = "독후감 일괄 승인", description = "선택한 독후감들을 일괄 승인 처리합니다.")
    @PatchMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveReports(@RequestBody ReportIdsRequest request) {
        return ResponseEntity.ok(reportService.approveReports(request.getReportIds()));
    }

    @Operation(summary = "독후감 일괄 보완 요청", description = "선택한 독후감들을 일괄 보완 요청 처리합니다. 보완 사유를 함께 입력할 수 있습니다.")
    @PatchMapping("/supplement")
    public ResponseEntity<Map<String, Object>> supplementReports(@RequestBody SupplementRequest request) {
        return ResponseEntity.ok(reportService.supplementReports(request.getReportIds(), request.getReason()));
    }
}
