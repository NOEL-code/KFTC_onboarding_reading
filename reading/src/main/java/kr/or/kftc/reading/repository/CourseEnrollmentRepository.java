package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndUserId(Long courseId, Long userId);
    List<CourseEnrollment> findByCourseId(Long courseId);
    List<CourseEnrollment> findByUserId(Long userId);
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
    boolean existsByCourseId(Long courseId);
    long countByCourseId(Long courseId);
    void deleteByCourseIdAndUserId(Long courseId, Long userId);
}
