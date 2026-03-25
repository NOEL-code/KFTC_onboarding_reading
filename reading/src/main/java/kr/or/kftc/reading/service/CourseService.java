package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.*;
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
                .map(c -> CourseListResponse.CourseItem.builder()
                        .courseId(c.getId())
                        .name(c.getName())
                        .status(c.getStatus())
                        .build())
                .toList();
        return CourseListResponse.builder().courses(items).build();
    }

    @Transactional
    public Map<String, Object> createCourse(CourseCreateRequest request) {
        ReadingCourse course = ReadingCourse.builder()
                .name(request.getName())
                .startDate(LocalDate.parse(request.getStartDate()))
                .endDate(LocalDate.parse(request.getEndDate()))
                .description(request.getDescription())
                .status("진행중")
                .build();
        courseRepository.save(course);
        return Map.of("courseId", course.getId(), "status", course.getStatus());
    }

    @Transactional
    public Map<String, Object> updateCourse(Long courseId, CourseUpdateRequest request) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        if (request.getName() != null) course.setName(request.getName());
        if (request.getStartDate() != null) course.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.getEndDate() != null) course.setEndDate(LocalDate.parse(request.getEndDate()));
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getStatus() != null) course.setStatus(request.getStatus());

        return Map.of("courseId", course.getId(), "message", "독서과정이 수정되었습니다.");
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
