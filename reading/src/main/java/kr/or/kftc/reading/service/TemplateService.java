package kr.or.kftc.reading.service;

import kr.or.kftc.reading.entity.Admin;
import kr.or.kftc.reading.entity.ReportTemplate;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.AdminRepository;
import kr.or.kftc.reading.repository.ReportTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateService {

    private final ReportTemplateRepository templateRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public Map<String, Object> uploadTemplate(MultipartFile file, Long adminId) {
        String fileName = file.getOriginalFilename();
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (!lower.endsWith(".hwp") && !lower.endsWith(".hwpx")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "hwp 또는 hwpx 파일만 업로드 가능합니다.");
        }

        // 기존 사용중 양식을 미사용으로 변경
        templateRepository.findByStatus("사용중")
                .ifPresent(t -> t.setStatus("미사용"));

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "관리자를 찾을 수 없습니다."));

        try {
            ReportTemplate template = ReportTemplate.builder()
                    .fileName(fileName)
                    .fileData(file.getBytes())
                    .fileSize(file.getSize())
                    .uploadedBy(admin)
                    .status("사용중")
                    .uploadedAt(LocalDateTime.now())
                    .build();
            templateRepository.save(template);

            return Map.of(
                    "templateId", template.getId(),
                    "fileName", template.getFileName(),
                    "fileSize", template.getFileSize(),
                    "status", template.getStatus(),
                    "uploadedAt", template.getUploadedAt().toString()
            );
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        ReportTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "양식을 찾을 수 없습니다."));
        templateRepository.delete(template);
    }

    public ReportTemplate getActiveTemplate() {
        return templateRepository.findByStatus("사용중")
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용 가능한 양식이 없습니다."));
    }

    public List<Map<String, Object>> getTemplateInfoList() {
        return templateRepository.findAll().stream()
                .map(t -> Map.<String, Object>of(
                        "templateId", t.getId(),
                        "fileName", t.getFileName(),
                        "fileSize", t.getFileSize(),
                        "status", t.getStatus(),
                        "uploadedAt", t.getUploadedAt() != null ? t.getUploadedAt().toString() : ""
                ))
                .toList();
    }

    public ReportTemplate getTemplateById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "양식을 찾을 수 없습니다."));
    }
}
