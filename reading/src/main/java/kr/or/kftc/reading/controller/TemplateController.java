package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.entity.ReportTemplate;
import kr.or.kftc.reading.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "사용자 - 양식", description = "독후감 양식 다운로드 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(summary = "양식 정보 조회", description = "현재 사용중인 양식의 메타정보를 조회합니다.")
    @GetMapping("/templates/info")
    public ResponseEntity<Map<String, Object>> getTemplateInfo() {
        ReportTemplate template = templateService.getActiveTemplate();
        return ResponseEntity.ok(Map.of(
                "fileName", template.getFileName(),
                "fileSize", template.getFileSize(),
                "uploadedAt", template.getUploadedAt() != null ? template.getUploadedAt().toString() : ""
        ));
    }

    @Operation(summary = "독후감 양식 다운로드", description = "현재 사용중인 HWP 양식 파일을 다운로드합니다.")
    @GetMapping("/templates/download")
    public ResponseEntity<byte[]> downloadTemplate() {
        ReportTemplate template = templateService.getActiveTemplate();

        String encodedFileName = URLEncoder.encode(template.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(template.getFileData());
    }
}
