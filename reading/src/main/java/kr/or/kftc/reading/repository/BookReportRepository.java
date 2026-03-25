package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.BookReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReportRepository extends JpaRepository<BookReport, Long> {

    Optional<BookReport> findByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollmentId(Long enrollmentId);

    @Query("SELECT br FROM BookReport br " +
           "JOIN br.enrollment e " +
           "JOIN e.course c " +
           "JOIN e.user u " +
           "WHERE c.id = :courseId")
    Page<BookReport> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT br FROM BookReport br " +
           "JOIN br.enrollment e " +
           "JOIN e.user u " +
           "WHERE u.employeeNo = :employeeNo")
    List<BookReport> findByUserEmployeeNo(@Param("employeeNo") String employeeNo);

    @Query("SELECT br FROM BookReport br " +
           "JOIN br.enrollment e " +
           "JOIN e.user u " +
           "WHERE u.name LIKE %:name%")
    List<BookReport> findByUserNameContaining(@Param("name") String name);

    @Query("SELECT br FROM BookReport br " +
           "JOIN br.enrollment e " +
           "JOIN e.user u " +
           "WHERE u.employeeNo = :employeeNo OR u.name LIKE %:name%")
    List<BookReport> findByUserEmployeeNoOrNameContaining(
            @Param("employeeNo") String employeeNo,
            @Param("name") String name);

    boolean existsByEnrollment_CourseId(Long courseId);
}
