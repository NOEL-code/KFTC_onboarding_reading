package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.CourseListResponse;
import kr.or.kftc.reading.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // API 4: GET /api/courses — 독서과정 목록 (드롭다운)
    @GetMapping("/courses")
    public ResponseEntity<CourseListResponse> getCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
}
