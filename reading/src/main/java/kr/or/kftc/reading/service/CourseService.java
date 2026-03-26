package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.entity.CourseStatus;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.BookReportRepository;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final ReadingCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final BookReportRepository reportRepository;

    public CourseListResponse getAllCourses() {
        List<ReadingCourse> courses = courseRepository.findAllByOrderByStartDateDesc();
        List<CourseListResponse.CourseItem> items = courses.stream()
                .map(c -> {
                    long totalUsers = enrollmentRepository.countByCourseId(c.getId());
                    long submittedUsers = reportRepository.countSubmittedByCourseId(c.getId());
                    return CourseListResponse.CourseItem.builder()
                            .courseId(c.getId())
                            .name(c.getName())
                            .description(c.getDescription() != null ? c.getDescription() : "")
                            .status(c.getStatus().name())
                            .startDate(c.getStartDate().toString())
                            .endDate(c.getEndDate().toString())
                            .totalUsers(totalUsers)
                            .submittedUsers(submittedUsers)
                            .build();
                })
                .toList();
        return CourseListResponse.builder().courses(items).build();
    }

    public CourseDetailResponse getCourseDetail(Long courseId) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        return CourseDetailResponse.builder()
                .courseId(course.getId())
                .name(course.getName())
                .status(course.getStatus().name())
                .startDate(course.getStartDate().toString())
                .endDate(course.getEndDate().toString())
                .description(course.getDescription())
                .build();
    }

    @Transactional
    public Map<String, Object> createCourse(CourseCreateRequest request) {
        ReadingCourse course = ReadingCourse.builder()
                .name(request.getName())
                .startDate(LocalDate.parse(request.getStartDate()))
                .endDate(LocalDate.parse(request.getEndDate()))
                .description(request.getDescription())
                .status(CourseStatus.진행중)
                .build();
        courseRepository.save(course);
        return Map.of("courseId", course.getId(), "status", course.getStatus().name());
    }

    @Transactional
    public Map<String, Object> updateCourse(Long courseId, CourseUpdateRequest request) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        if (request.getName() != null) course.setName(request.getName());
        if (request.getStartDate() != null) course.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.getEndDate() != null) course.setEndDate(LocalDate.parse(request.getEndDate()));
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            try {
                course.setStatus(CourseStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다: " + request.getStatus());
            }
        }

        return Map.of("courseId", course.getId(), "message", "독서과정이 수정되었습니다.");
    }

    @Transactional
    public Map<String, Object> updateCourseStatus(Long courseId, String status) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        try {
            course.setStatus(CourseStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다: " + status);
        }

        return Map.of("courseId", course.getId(), "status", course.getStatus().name(), "message", "독서과정 상태가 변경되었습니다.");
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        if (enrollmentRepository.existsByCourseId(courseId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "해당 과정에 등록된 사용자가 있어 삭제할 수 없습니다.");
        }

        courseRepository.delete(course);
    }
}
