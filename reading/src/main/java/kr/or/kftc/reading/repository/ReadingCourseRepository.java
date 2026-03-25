package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.ReadingCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingCourseRepository extends JpaRepository<ReadingCourse, Long> {
    List<ReadingCourse> findAllByOrderByStartDateDesc();
}
