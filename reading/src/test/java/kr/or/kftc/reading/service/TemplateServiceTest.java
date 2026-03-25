package kr.or.kftc.reading.service;

import kr.or.kftc.reading.entity.Admin;
import kr.or.kftc.reading.entity.ReportTemplate;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.AdminRepository;
import kr.or.kftc.reading.repository.ReportTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock private ReportTemplateRepository templateRepository;
    @Mock private AdminRepository adminRepository;
    @InjectMocks private TemplateService templateService;

    @Test
    @DisplayName("양식 업로드 성공 - 기존 양식 미사용 처리")
    void uploadTemplateSuccess() {
        ReportTemplate existing = ReportTemplate.builder().id(1L).status("사용중").build();
        Admin admin = Admin.builder().id(1L).name("관리자1").build();

        when(templateRepository.findByStatus("사용중")).thenReturn(Optional.of(existing));
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(templateRepository.save(any(ReportTemplate.class))).thenAnswer(inv -> {
            ReportTemplate t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        MockMultipartFile file = new MockMultipartFile("file", "양식.hwp", "application/hwp", "data".getBytes());
        Map<String, Object> result = templateService.uploadTemplate(file, 1L);

        assertThat(result.get("templateId")).isEqualTo(2L);
        assertThat(result.get("status")).isEqualTo("사용중");
        assertThat(existing.getStatus()).isEqualTo("미사용"); // 기존 양식 상태 변경됨
    }

    @Test
    @DisplayName("양식 업로드 실패 - hwp 아닌 파일")
    void uploadTemplateInvalidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "양식.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> templateService.uploadTemplate(file, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hwp 파일만 업로드");
    }

    @Test
    @DisplayName("양식 삭제 성공")
    void deleteTemplate() {
        ReportTemplate template = ReportTemplate.builder().id(1L).build();
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        templateService.deleteTemplate(1L);

        verify(templateRepository).delete(template);
    }

    @Test
    @DisplayName("양식 삭제 실패 - 없음")
    void deleteTemplateNotFound() {
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.deleteTemplate(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("활성 양식 조회 성공")
    void getActiveTemplate() {
        ReportTemplate template = ReportTemplate.builder().id(1L).status("사용중").fileName("양식.hwp").build();
        when(templateRepository.findByStatus("사용중")).thenReturn(Optional.of(template));

        ReportTemplate result = templateService.getActiveTemplate();

        assertThat(result.getFileName()).isEqualTo("양식.hwp");
    }

    @Test
    @DisplayName("활성 양식 없을 때 예외")
    void getActiveTemplateNotFound() {
        when(templateRepository.findByStatus("사용중")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.getActiveTemplate())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용 가능한 양식이 없습니다");
    }
}
