package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.CourseCreateRequest;
import kr.or.kftc.reading.dto.CourseListResponse;
import kr.or.kftc.reading.dto.CourseUpdateRequest;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.BookReportRepository;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private ReadingCourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private BookReportRepository reportRepository;
    @InjectMocks private CourseService courseService;

    @Test
    @DisplayName("독서과정 목록 조회")
    void getAllCourses() {
        ReadingCourse c1 = ReadingCourse.builder().id(1L).name("26년 상반기").status("진행중").build();
        ReadingCourse c2 = ReadingCourse.builder().id(2L).name("25년 하반기").status("완료").build();
        when(courseRepository.findAllByOrderByStartDateDesc()).thenReturn(List.of(c1, c2));

        CourseListResponse response = courseService.getAllCourses();

        assertThat(response.getCourses()).hasSize(2);
        assertThat(response.getCourses().get(0).getName()).isEqualTo("26년 상반기");
    }

    @Test
    @DisplayName("독서과정 생성 성공")
    void createCourse() {
        when(courseRepository.save(any(ReadingCourse.class))).thenAnswer(inv -> {
            ReadingCourse c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("26년 상반기");
        request.setStartDate("2026-01-01");
        request.setEndDate("2026-06-30");
        request.setDescription("설명");

        Map<String, Object> result = courseService.createCourse(request);

        assertThat(result.get("courseId")).isEqualTo(1L);
        assertThat(result.get("status")).isEqualTo("진행중");
    }

    @Test
    @DisplayName("독서과정 수정 성공")
    void updateCourse() {
        ReadingCourse course = ReadingCourse.builder()
                .id(1L).name("26년 상반기")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status("진행중").build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setName("수정된 과정명");
        request.setStatus("완료");

        Map<String, Object> result = courseService.updateCourse(1L, request);

        assertThat(result.get("message")).isEqualTo("독서과정이 수정되었습니다.");
        assertThat(course.getName()).isEqualTo("수정된 과정명");
        assertThat(course.getStatus()).isEqualTo("완료");
    }

    @Test
    @DisplayName("독서과정 수정 실패 - 과정 없음")
    void updateCourseNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setName("이름");

        assertThatThrownBy(() -> courseService.updateCourse(999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("독서과정 삭제 성공")
    void deleteCourse() {
        ReadingCourse course = ReadingCourse.builder().id(1L).name("26년 상반기").build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseId(1L)).thenReturn(false);

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("독서과정 삭제 실패 - 등록자 존재")
    void deleteCourseConflict() {
        ReadingCourse course = ReadingCourse.builder().id(1L).name("26년 상반기").build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseId(1L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.deleteCourse(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
