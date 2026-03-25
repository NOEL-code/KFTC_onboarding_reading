package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.MessageResponse;
import kr.or.kftc.reading.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {

    private final TemplateService templateService;

    // API 14: POST /api/admin/templates — 양식 업로드/교체
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.uploadTemplate(file, adminId));
    }

    // API 15: DELETE /api/admin/templates/{templateId} — 양식 삭제
    @DeleteMapping("/{templateId}")
    public ResponseEntity<MessageResponse> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(new MessageResponse("양식이 삭제되었습니다."));
    }
}
