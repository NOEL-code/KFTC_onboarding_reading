package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.UserCreateRequest;
import kr.or.kftc.reading.dto.UserUpdateRequest;
import kr.or.kftc.reading.entity.CourseEnrollment;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.entity.User;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import kr.or.kftc.reading.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ReadingCourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("사용자 개별 추가 - 신규 사용자")
    void createNewUser() {
        ReadingCourse course = ReadingCourse.builder().id(1L).name("26년 상반기").build();
        User user = User.builder().id(1L).employeeNo("20210001").name("김민수").department("IT개발부").build();
        CourseEnrollment enrollment = CourseEnrollment.builder().id(1L).course(course).user(user).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenReturn(enrollment);

        UserCreateRequest request = new UserCreateRequest();
        request.setCourseId(1L);
        request.setEmployeeNo("20210001");
        request.setName("김민수");
        request.setDepartment("IT개발부");

        Map<String, Object> result = userService.createUser(request);

        assertThat(result.get("userId")).isEqualTo(1L);
        assertThat(result.get("enrollmentId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 개별 추가 - 기존 사용자 재등록")
    void createExistingUser() {
        ReadingCourse course = ReadingCourse.builder().id(1L).build();
        User user = User.builder().id(1L).employeeNo("20210001").name("김민수").department("IT개발부").build();
        CourseEnrollment enrollment = CourseEnrollment.builder().id(2L).course(course).user(user).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.of(user));
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenReturn(enrollment);

        UserCreateRequest request = new UserCreateRequest();
        request.setCourseId(1L);
        request.setEmployeeNo("20210001");
        request.setName("김민수");
        request.setDepartment("IT개발부");

        Map<String, Object> result = userService.createUser(request);

        assertThat(result.get("userId")).isEqualTo(1L);
        verify(userRepository, never()).save(any(User.class)); // 새로 저장하지 않음
    }

    @Test
    @DisplayName("사용자 추가 실패 - 이미 등록된 과정")
    void createUserDuplicate() {
        ReadingCourse course = ReadingCourse.builder().id(1L).build();
        User user = User.builder().id(1L).employeeNo("20210001").name("김민수").department("IT개발부").build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.of(user));
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(true);

        UserCreateRequest request = new UserCreateRequest();
        request.setCourseId(1L);
        request.setEmployeeNo("20210001");
        request.setName("김민수");
        request.setDepartment("IT개발부");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUser() {
        User user = User.builder().id(1L).employeeNo("20210001").name("김민수").department("IT개발부").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("김민수(수정)");
        request.setDepartment("경영지원부");

        Map<String, Object> result = userService.updateUser(1L, request);

        assertThat(result.get("message")).isEqualTo("사용자 정보가 수정되었습니다.");
        assertThat(user.getName()).isEqualTo("김민수(수정)");
        assertThat(user.getDepartment()).isEqualTo("경영지원부");
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser() {
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(true);

        userService.deleteUser(1L, 1L);

        verify(enrollmentRepository).deleteByCourseIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 해당 과정에 등록 안됨")
    void deleteUserNotFound() {
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
