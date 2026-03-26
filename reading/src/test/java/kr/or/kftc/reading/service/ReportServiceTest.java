package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.entity.*;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.*;
import static kr.or.kftc.reading.entity.CourseStatus.*;
import static kr.or.kftc.reading.entity.ReportStatus.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private BookReportRepository reportRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReadingCourseRepository courseRepository;
    @InjectMocks private ReportService reportService;

    private User createUser() {
        return User.builder().id(1L).employeeNo("20210001").name("김민수").team("IT개발팀").build();
    }

    private ReadingCourse createCourse() {
        return ReadingCourse.builder().id(1L).name("26년 상반기")
                .startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 6, 30))
                .status(진행중).build();
    }

    private CourseEnrollment createEnrollment(ReadingCourse course, User user) {
        return CourseEnrollment.builder().id(1L).course(course).user(user).build();
    }

    private BookReport createReport(CourseEnrollment enrollment) {
        return BookReport.builder().id(1L).enrollment(enrollment)
                .title("독후감1").fileName("report.hwp").fileSize(1024L)
                .status(제출).submittedAt(LocalDateTime.now()).build();
    }

    // === 독후감 제출 ===

    @Test
    @DisplayName("독후감 제출 성공")
    void submitReportSuccess() {
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(createCourse(), user);
        MockMultipartFile file = new MockMultipartFile("file", "report.hwp", "application/hwp", "data".getBytes());

        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.of(user));
        when(enrollmentRepository.findByCourseIdAndUserId(1L, 1L)).thenReturn(Optional.of(enrollment));
        when(reportRepository.existsByEnrollmentId(1L)).thenReturn(false);
        when(reportRepository.save(any(BookReport.class))).thenAnswer(inv -> {
            BookReport r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReportResponse response = reportService.submitReport(1L, "20210001", "독후감1", file);

        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("제출");
        assertThat(response.getSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("독후감 제출 실패 - 사용자 없음")
    void submitReportUserNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "report.hwp", "application/hwp", "data".getBytes());
        when(userRepository.findByEmployeeNo("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.submitReport(1L, "99999", "제목", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("독후감 제출 실패 - 이미 제출됨")
    void submitReportAlreadyExists() {
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(createCourse(), user);
        MockMultipartFile file = new MockMultipartFile("file", "report.hwp", "application/hwp", "data".getBytes());

        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.of(user));
        when(enrollmentRepository.findByCourseIdAndUserId(1L, 1L)).thenReturn(Optional.of(enrollment));
        when(reportRepository.existsByEnrollmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reportService.submitReport(1L, "20210001", "제목", file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("독후감 제출 실패 - hwp 아닌 파일")
    void submitReportInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> reportService.submitReport(1L, "20210001", "제목", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hwp 파일만 업로드");
    }

    // === 독후감 목록 조회 ===

    @Test
    @DisplayName("과정별 독후감 목록 조회 성공")
    void getReportsByCourse() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reportRepository.findByCourseId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(report), PageRequest.of(0, 10), 1));

        ReportListResponse response = reportService.getReportsByCourse(1L, 1, 10);

        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getReports()).hasSize(1);
        assertThat(response.getReports().get(0).getName()).isEqualTo("김민수");
    }

    // === 독후감 상세 조회 ===

    @Test
    @DisplayName("독후감 상세 조회 성공")
    void getReportDetail() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportDetailResponse response = reportService.getReportDetail(1L);

        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(response.getCourseName()).isEqualTo("26년 상반기");
        assertThat(response.getEmployeeNo()).isEqualTo("20210001");
        assertThat(response.getName()).isEqualTo("김민수");
    }

    @Test
    @DisplayName("독후감 상세 조회 실패 - 없음")
    void getReportDetailNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReportDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // === 독후감 검색 ===

    @Test
    @DisplayName("사번으로 독후감 검색")
    void searchByEmployeeNo() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findByUserEmployeeNo("20210001")).thenReturn(List.of(report));

        ReportSearchResponse response = reportService.searchReports("20210001", null);

        assertThat(response.getResults()).hasSize(1);
    }

    @Test
    @DisplayName("검색 실패 - 사번, 이름 모두 없음")
    void searchNoParams() {
        assertThatThrownBy(() -> reportService.searchReports(null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사번 또는 이름 중 하나는 필수");
    }

    // === 독후감 수정 ===

    @Test
    @DisplayName("독후감 수정 성공")
    void updateReportSuccess() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportResponse response = reportService.updateReport(1L, "20210001", "수정된 제목", null);

        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(report.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("독후감 수정 실패 - 본인 아님")
    void updateReportForbidden() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.updateReport(1L, "99999", "제목", null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보완 상태 독후감 재제출 시 제출 상태로 변경")
    void resubmitSupplementReport() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);
        report.setStatus(보완);

        MockMultipartFile file = new MockMultipartFile("file", "new.hwp", "application/hwp", "data".getBytes());
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportResponse response = reportService.updateReport(1L, "20210001", null, file);

        assertThat(report.getStatus()).isEqualTo(제출);
    }

    // === 독후감 삭제 ===

    @Test
    @DisplayName("독후감 삭제 성공")
    void deleteReportSuccess() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        reportService.deleteReport(1L, "20210001");

        verify(reportRepository).delete(report);
    }

    @Test
    @DisplayName("독후감 삭제 실패 - 본인 아님")
    void deleteReportForbidden() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.deleteReport(1L, "99999"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    // === 관리자: 일괄 승인 ===

    @Test
    @DisplayName("일괄 승인 성공")
    void approveReports() {
        BookReport r1 = BookReport.builder().id(1L).status(제출).build();
        BookReport r2 = BookReport.builder().id(2L).status(제출).build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(r1));
        when(reportRepository.findById(2L)).thenReturn(Optional.of(r2));

        Map<String, Object> result = reportService.approveReports(List.of(1L, 2L));

        assertThat(result.get("updatedCount")).isEqualTo(2);
        assertThat(r1.getStatus()).isEqualTo(승인);
        assertThat(r2.getStatus()).isEqualTo(승인);
    }

    // === 관리자: 일괄 보완 ===

    @Test
    @DisplayName("일괄 보완 요청 성공")
    void supplementReports() {
        BookReport r1 = BookReport.builder().id(1L).status(제출).build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(r1));

        Map<String, Object> result = reportService.supplementReports(List.of(1L), "내용 보완 필요");

        assertThat(result.get("updatedCount")).isEqualTo(1);
        assertThat(r1.getStatus()).isEqualTo(보완);
        assertThat(r1.getSupplementReason()).isEqualTo("내용 보완 필요");
    }

    // === 관리자: 독후감 목록 ===

    @Test
    @DisplayName("관리자 독후감 목록 조회 - summary 포함")
    void getAdminReports() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);

        when(enrollmentRepository.findByCourseId(1L)).thenReturn(List.of(enrollment));
        when(reportRepository.findByEnrollmentId(1L)).thenReturn(Optional.of(report));

        AdminReportListResponse response = reportService.getAdminReports(1L, null, null, null, 1, 10);

        assertThat(response.getSummary().getTotal()).isEqualTo(1);
        assertThat(response.getSummary().getSubmitted()).isEqualTo(1);
        assertThat(response.getReports()).hasSize(1);
    }

    @Test
    @DisplayName("관리자 독후감 목록 - 상태 필터링")
    void getAdminReportsWithStatusFilter() {
        ReadingCourse course = createCourse();
        User user = createUser();
        CourseEnrollment enrollment = createEnrollment(course, user);
        BookReport report = createReport(enrollment);
        report.setStatus(승인);

        when(enrollmentRepository.findByCourseId(1L)).thenReturn(List.of(enrollment));
        when(reportRepository.findByEnrollmentId(1L)).thenReturn(Optional.of(report));

        AdminReportListResponse response = reportService.getAdminReports(1L, "제출", null, null, 1, 10);

        assertThat(response.getReports()).isEmpty(); // 승인 상태이므로 "제출" 필터에 안 걸림
    }
}
