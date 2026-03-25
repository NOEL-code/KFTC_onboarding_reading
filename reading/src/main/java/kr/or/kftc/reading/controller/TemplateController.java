package kr.or.kftc.reading.controller;

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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    // API 8: GET /api/templates/download — 양식 다운로드
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
