package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.entity.*;
import kr.or.kftc.reading.exception.BusinessException;
import static kr.or.kftc.reading.entity.ReportStatus.*;
import kr.or.kftc.reading.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final BookReportRepository reportRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ReadingCourseRepository courseRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public ReportResponse submitReport(Long courseId, String employeeNo, String title, MultipartFile file) {
        validateHwpFile(file);

        User user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 과정에 등록된 사용자가 아닙니다."));

        if (reportRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 제출된 독후감이 존재합니다.");
        }

        try {
            BookReport report = BookReport.builder()
                    .enrollment(enrollment)
                    .title(title)
                    .fileName(file.getOriginalFilename())
                    .fileData(file.getBytes())
                    .fileSize(file.getSize())
                    .status(제출)
                    .submittedAt(LocalDateTime.now())
                    .build();
            reportRepository.save(report);

            return ReportResponse.builder()
                    .reportId(report.getId())
                    .status(report.getStatus().name())
                    .submittedAt(report.getSubmittedAt())
                    .build();
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
        }
    }

    public ReportListResponse getReportsByCourse(Long courseId, int page, int size) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        // 모든 수강생을 포함 (미제출자 포함) — User를 JOIN FETCH로 즉시 로딩
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseIdWithUser(courseId);

        List<ReportListResponse.ReportSummary> allSummaries = enrollments.stream()
                .map(e -> {
                    BookReport report = reportRepository.findByEnrollmentId(e.getId()).orElse(null);
                    User u = e.getUser();
                    String reportStatus = report != null ? report.getStatus().name() : 미제출.name();
                    return ReportListResponse.ReportSummary.builder()
                            .reportId(report != null ? report.getId() : null)
                            .employeeNo(u.getEmployeeNo())
                            .name(u.getName())
                            .team(u.getTeam())
                            .title(report != null ? report.getTitle() : null)
                            .status(reportStatus)
                            .submittedAt(report != null && report.getSubmittedAt() != null
                                    ? report.getSubmittedAt().format(DATE_FMT) : null)
                            .build();
                })
                .toList();

        // 페이지네이션
        int totalPages = (int) Math.ceil((double) allSummaries.size() / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allSummaries.size());
        List<ReportListResponse.ReportSummary> pageItems =
                fromIndex < allSummaries.size() ? allSummaries.subList(fromIndex, toIndex) : List.of();

        return ReportListResponse.builder()
                .totalCount(allSummaries.size())
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .reports(pageItems)
                .build();
    }

    public ReportDetailResponse getReportDetail(Long reportId) {
        BookReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."));

        User user = report.getEnrollment().getUser();
        ReadingCourse course = report.getEnrollment().getCourse();

        return ReportDetailResponse.builder()
                .reportId(report.getId())
                .courseName(course.getName())
                .employeeNo(user.getEmployeeNo())
                .name(user.getName())
                .team(user.getTeam())
                .title(report.getTitle())
                .fileName(report.getFileName())
                .fileSize(report.getFileSize())
                .status(report.getStatus().name())
                .supplementReason(report.getSupplementReason())
                .submittedAt(report.getSubmittedAt())
                .downloadUrl("/api/reports/" + report.getId() + "/download")
                .build();
    }

    @Transactional
    public ReportResponse updateReport(Long reportId, String employeeNo, String title, MultipartFile file) {
        BookReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."));

        User user = report.getEnrollment().getUser();
        if (!user.getEmployeeNo().equals(employeeNo)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 독후감만 수정할 수 있습니다.");
        }

        report.setTitle(title);

        if (file != null && !file.isEmpty()) {
            validateHwpFile(file);
            try {
                report.setFileName(file.getOriginalFilename());
                report.setFileData(file.getBytes());
                report.setFileSize(file.getSize());
            } catch (IOException e) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
            }
        }

        // 보완 상태에서 재제출 시 "제출"로 변경
        if (report.getStatus() == 보완) {
            report.setStatus(제출);
            report.setSubmittedAt(LocalDateTime.now());
            report.setSupplementReason(null);
        }

        return ReportResponse.builder()
                .reportId(report.getId())
                .status(report.getStatus().name())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteReport(Long reportId, String employeeNo) {
        BookReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."));

        User user = report.getEnrollment().getUser();
        if (!user.getEmployeeNo().equals(employeeNo)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 독후감만 삭제할 수 있습니다.");
        }

        reportRepository.delete(report);
    }

    // === 관리자 기능 ===

    public AdminReportListResponse getAdminReports(Long courseId, String status, String team,
                                                    String name, int page, int size) {
        // 전체 enrollment 조회 (해당 과정) — User를 JOIN FETCH로 즉시 로딩
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseIdWithUser(courseId);

        // enrollment별로 report 매핑
        List<AdminReportListResponse.AdminReportItem> allItems = enrollments.stream()
                .map(e -> {
                    BookReport report = reportRepository.findByEnrollmentId(e.getId()).orElse(null);
                    User u = e.getUser();
                    String reportStatus = report != null ? report.getStatus().name() : 미제출.name();
                    return AdminReportListResponse.AdminReportItem.builder()
                            .reportId(report != null ? report.getId() : null)
                            .enrollmentId(e.getId())
                            .employeeNo(u.getEmployeeNo())
                            .name(u.getName())
                            .team(u.getTeam())
                            .title(report != null ? report.getTitle() : null)
                            .status(reportStatus)
                            .supplementReason(report != null ? report.getSupplementReason() : null)
                            .submittedAt(report != null && report.getSubmittedAt() != null
                                    ? report.getSubmittedAt().format(DATE_FMT) : null)
                            .build();
                })
                .toList();

        // 필터링
        List<AdminReportListResponse.AdminReportItem> filtered = allItems.stream()
                .filter(item -> status == null || "전체".equals(status) || item.getStatus().equals(status))
                .filter(item -> team == null || team.isBlank() || item.getTeam().equals(team))
                .filter(item -> name == null || name.isBlank() || item.getName().contains(name))
                .toList();

        // Summary 계산
        long total = allItems.size();
        long submitted = allItems.stream().filter(i -> 제출.name().equals(i.getStatus())).count();
        long approved = allItems.stream().filter(i -> 승인.name().equals(i.getStatus())).count();
        long supplement = allItems.stream().filter(i -> 보완.name().equals(i.getStatus())).count();
        long notSubmitted = allItems.stream().filter(i -> 미제출.name().equals(i.getStatus())).count();

        // 페이지네이션
        int totalPages = (int) Math.ceil((double) filtered.size() / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<AdminReportListResponse.AdminReportItem> pageItems =
                fromIndex < filtered.size() ? filtered.subList(fromIndex, toIndex) : List.of();

        return AdminReportListResponse.builder()
                .summary(AdminReportListResponse.Summary.builder()
                        .total(total)
                        .submitted(submitted)
                        .approved(approved)
                        .supplement(supplement)
                        .notSubmitted(notSubmitted)
                        .build())
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .reports(pageItems)
                .build();
    }

    public BookReport getReportEntity(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."));
    }

    @Transactional
    public Map<String, Object> approveReports(List<Long> reportIds) {
        int count = 0;
        for (Long id : reportIds) {
            BookReport report = reportRepository.findById(id).orElse(null);
            if (report != null && report.getStatus() != 미제출) {
                report.setStatus(승인);
                count++;
            }
        }
        return Map.of("updatedCount", count, "message", count + "건이 승인되었습니다.");
    }

    @Transactional
    public Map<String, Object> supplementReports(List<Long> reportIds, String reason) {
        int count = 0;
        for (Long id : reportIds) {
            BookReport report = reportRepository.findById(id).orElse(null);
            if (report != null && report.getStatus() != 미제출) {
                report.setStatus(보완);
                report.setSupplementReason(reason);
                count++;
            }
        }
        return Map.of("updatedCount", count, "message", count + "건이 보완 요청되었습니다.");
    }

    private void validateHwpFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "파일을 선택해주세요.");
        }
        String fileName = file.getOriginalFilename();
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (!lower.endsWith(".hwp") && !lower.endsWith(".hwpx")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "유효하지 않은 파일 형식입니다. .hwp 또는 .hwpx 파일만 업로드 가능합니다.");
        }
    }
}
