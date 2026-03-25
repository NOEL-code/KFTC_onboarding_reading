package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class BookReportRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookReportRepository reportRepository;

    private ReadingCourse course;
    private User user1;
    private User user2;
    private CourseEnrollment enrollment1;
    private CourseEnrollment enrollment2;

    @BeforeEach
    void setUp() {
        course = em.persist(ReadingCourse.builder()
                .name("26년 상반기")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status("진행중")
                .build());

        user1 = em.persist(User.builder()
                .employeeNo("20210001")
                .name("김민수")
                .department("IT개발부")
                .build());

        user2 = em.persist(User.builder()
                .employeeNo("20210002")
                .name("이영희")
                .department("인사부")
                .build());

        enrollment1 = em.persist(CourseEnrollment.builder()
                .course(course).user(user1).build());
        enrollment2 = em.persist(CourseEnrollment.builder()
                .course(course).user(user2).build());

        em.persist(BookReport.builder()
                .enrollment(enrollment1)
                .title("독후감1")
                .fileName("report1.hwp")
                .fileSize(1024L)
                .status("제출")
                .submittedAt(LocalDateTime.now())
                .build());

        em.persist(BookReport.builder()
                .enrollment(enrollment2)
                .title("독후감2")
                .fileName("report2.hwp")
                .fileSize(2048L)
                .status("승인")
                .submittedAt(LocalDateTime.now())
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("과정 ID로 독후감 목록 페이징 조회")
    void findByCourseId() {
        Page<BookReport> result = reportRepository.findByCourseId(course.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("사번으로 독후감 검색")
    void findByUserEmployeeNo() {
        List<BookReport> result = reportRepository.findByUserEmployeeNo("20210001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("독후감1");
    }

    @Test
    @DisplayName("이름으로 독후감 검색")
    void findByUserNameContaining() {
        List<BookReport> result = reportRepository.findByUserNameContaining("영희");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("독후감2");
    }

    @Test
    @DisplayName("enrollment ID로 독후감 존재 여부 확인")
    void existsByEnrollmentId() {
        assertThat(reportRepository.existsByEnrollmentId(enrollment1.getId())).isTrue();
    }

    @Test
    @DisplayName("enrollment ID로 독후감 조회")
    void findByEnrollmentId() {
        var result = reportRepository.findByEnrollmentId(enrollment1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("독후감1");
    }

    @Test
    @DisplayName("과정에 독후감이 존재하는지 확인")
    void existsByEnrollment_CourseId() {
        assertThat(reportRepository.existsByEnrollment_CourseId(course.getId())).isTrue();
    }
}
