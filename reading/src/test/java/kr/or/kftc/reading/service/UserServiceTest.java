package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.UserBatchResponse;
import kr.or.kftc.reading.dto.UserCreateRequest;
import kr.or.kftc.reading.dto.UserUpdateRequest;
import kr.or.kftc.reading.entity.CourseEnrollment;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.entity.User;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import kr.or.kftc.reading.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @DisplayName("일괄 추가 - 신규 사용자 2명 성공")
    void createUsers() {
        ReadingCourse course = ReadingCourse.builder().id(1L).name("26년 상반기").build();
        User user1 = User.builder().id(1L).employeeNo("20210001").name("김민수").build();
        User user2 = User.builder().id(2L).employeeNo("20210002").name("이영희").build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNo("20210002")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user1, user2);
        when(enrollmentRepository.existsByCourseIdAndUserId(eq(1L), any())).thenReturn(false);
        when(enrollmentRepository.save(any(CourseEnrollment.class)))
                .thenReturn(CourseEnrollment.builder().id(1L).build());

        UserCreateRequest req1 = new UserCreateRequest();
        req1.setCourseId(1L); req1.setEmployeeNo("20210001"); req1.setName("김민수"); req1.setDepartment("IT개발부");
        UserCreateRequest req2 = new UserCreateRequest();
        req2.setCourseId(1L); req2.setEmployeeNo("20210002"); req2.setName("이영희"); req2.setDepartment("기획부");

        UserBatchResponse result = userService.createUsers(List.of(req1, req2));

        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("일괄 추가 - 일부 중복 시 부분 성공")
    void createUsersPartialFail() {
        ReadingCourse course = ReadingCourse.builder().id(1L).build();
        User user1 = User.builder().id(1L).employeeNo("20210001").name("김민수").build();
        User user2 = User.builder().id(2L).employeeNo("20210002").name("이영희").build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmployeeNo("20210001")).thenReturn(Optional.of(user1));
        when(userRepository.findByEmployeeNo("20210002")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user2);
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(true);
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 2L)).thenReturn(false);
        when(enrollmentRepository.save(any(CourseEnrollment.class)))
                .thenReturn(CourseEnrollment.builder().id(2L).build());

        UserCreateRequest req1 = new UserCreateRequest();
        req1.setCourseId(1L); req1.setEmployeeNo("20210001"); req1.setName("김민수"); req1.setDepartment("IT개발부");
        UserCreateRequest req2 = new UserCreateRequest();
        req2.setCourseId(1L); req2.setEmployeeNo("20210002"); req2.setName("이영희"); req2.setDepartment("기획부");

        UserBatchResponse result = userService.createUsers(List.of(req1, req2));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getResults().get(0).isSuccess()).isFalse();
        assertThat(result.getResults().get(1).isSuccess()).isTrue();
    }

    @Test
    @DisplayName("일괄 수정 - 2명 수정 성공")
    void updateUsers() {
        User user1 = User.builder().id(1L).employeeNo("20210001").name("김민수").department("IT개발부").build();
        User user2 = User.builder().id(2L).employeeNo("20210002").name("이영희").department("기획부").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        UserUpdateRequest req1 = new UserUpdateRequest();
        req1.setUserId(1L); req1.setName("김민수(수정)");
        UserUpdateRequest req2 = new UserUpdateRequest();
        req2.setUserId(2L); req2.setDepartment("경영지원부");

        UserBatchResponse result = userService.updateUsers(List.of(req1, req2));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(user1.getName()).isEqualTo("김민수(수정)");
        assertThat(user2.getDepartment()).isEqualTo("경영지원부");
    }

    @Test
    @DisplayName("일괄 수정 - 존재하지 않는 사용자 포함 시 부분 성공")
    void updateUsersPartialFail() {
        User user1 = User.builder().id(1L).name("김민수").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserUpdateRequest req1 = new UserUpdateRequest();
        req1.setUserId(1L); req1.setName("김민수(수정)");
        UserUpdateRequest req2 = new UserUpdateRequest();
        req2.setUserId(999L); req2.setName("없는사용자");

        UserBatchResponse result = userService.updateUsers(List.of(req1, req2));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("일괄 삭제 - 2명 삭제 성공")
    void deleteUsers() {
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(true);
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 2L)).thenReturn(true);

        UserBatchResponse result = userService.deleteUsers(1L, List.of(1L, 2L));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        verify(enrollmentRepository).deleteByCourseIdAndUserId(1L, 1L);
        verify(enrollmentRepository).deleteByCourseIdAndUserId(1L, 2L);
    }

    @Test
    @DisplayName("일괄 삭제 - 미등록 사용자 포함 시 부분 성공")
    void deleteUsersPartialFail() {
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 1L)).thenReturn(true);
        when(enrollmentRepository.existsByCourseIdAndUserId(1L, 999L)).thenReturn(false);

        UserBatchResponse result = userService.deleteUsers(1L, List.of(1L, 999L));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(1);
    }
}
