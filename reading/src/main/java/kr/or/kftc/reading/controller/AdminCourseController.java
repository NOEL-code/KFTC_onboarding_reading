package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.CourseCreateRequest;
import kr.or.kftc.reading.dto.CourseUpdateRequest;
import kr.or.kftc.reading.dto.MessageResponse;
import kr.or.kftc.reading.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    // API 16: POST /api/admin/courses — 독서과정 생성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody CourseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    // API 17: PUT /api/admin/courses/{courseId} — 독서과정 수정
    @PutMapping("/{courseId}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseUpdateRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    // API 18: DELETE /api/admin/courses/{courseId} — 독서과정 삭제
    @DeleteMapping("/{courseId}")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("독서과정이 삭제되었습니다."));
    }
}
