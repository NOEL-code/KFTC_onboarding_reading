package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.CourseEnrollment;
import kr.or.kftc.reading.entity.CourseStatus;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CourseEnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    private ReadingCourse course;
    private User user;

    @BeforeEach
    void setUp() {
        course = em.persist(ReadingCourse.builder()
                .name("26년 상반기")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(CourseStatus.진행중)
                .build());

        user = em.persist(User.builder()
                .employeeNo("20210001")
                .name("김민수")
                .team("IT개발팀")
                .build());

        em.persist(CourseEnrollment.builder()
                .course(course).user(user).build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("과정ID + 사용자ID로 enrollment 조회")
    void findByCourseIdAndUserId() {
        var result = enrollmentRepository.findByCourseIdAndUserId(course.getId(), user.getId());
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("과정ID로 enrollment 목록 조회")
    void findByCourseId() {
        List<CourseEnrollment> result = enrollmentRepository.findByCourseId(course.getId());
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("과정+사용자 중복 등록 확인")
    void existsByCourseIdAndUserId() {
        assertThat(enrollmentRepository.existsByCourseIdAndUserId(course.getId(), user.getId())).isTrue();
        assertThat(enrollmentRepository.existsByCourseIdAndUserId(course.getId(), 999L)).isFalse();
    }

    @Test
    @DisplayName("과정에 등록자가 존재하는지 확인")
    void existsByCourseId() {
        assertThat(enrollmentRepository.existsByCourseId(course.getId())).isTrue();
    }
}
