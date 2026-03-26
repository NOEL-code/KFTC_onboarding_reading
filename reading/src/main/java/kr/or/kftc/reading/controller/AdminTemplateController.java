package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.MessageResponse;
import kr.or.kftc.reading.entity.ReportTemplate;
import kr.or.kftc.reading.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "관리자 - 양식 관리", description = "독후감 양식 업로드/삭제 API (JWT 필요)")
@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {

    private final TemplateService templateService;

    @Operation(summary = "양식 목록 조회", description = "등록된 양식 정���를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTemplates() {
        return ResponseEntity.ok(templateService.getTemplateInfoList());
    }

    @Operation(summary = "양식 다운로드", description = "양식 파일을 다운로드합니다.")
    @GetMapping("/{templateId}/download")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Long templateId) {
        ReportTemplate template = templateService.getTemplateById(templateId);
        String encodedFileName = URLEncoder.encode(template.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(template.getFileData());
    }

    @Operation(summary = "양식 ���로드/교체", description = "새 HWP 양식을 업로���합니다. 기존 양���은 자동 비활성화됩니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.uploadTemplate(file, adminId));
    }

    @Operation(summary = "양식 삭제", description = "양식을 삭제합니다.")
    @DeleteMapping("/{templateId}")
    public ResponseEntity<MessageResponse> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(new MessageResponse("양식이 삭제되었습니다."));
    }
}
