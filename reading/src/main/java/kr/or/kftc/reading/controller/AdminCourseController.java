package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.CourseCreateRequest;
import kr.or.kftc.reading.dto.CourseUpdateRequest;
import kr.or.kftc.reading.dto.MessageResponse;
import kr.or.kftc.reading.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "관리자 - 독서과정 관리", description = "독서과정 생성/수정/삭제 API (JWT 필요)")
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    @Operation(summary = "독서과정 생성", description = "새 독서과정을 생성합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody CourseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @Operation(summary = "독서과정 수정", description = "독서과정 정보를 수정합니다.")
    @PutMapping("/{courseId}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseUpdateRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @Operation(summary = "독서과정 삭제", description = "독서과정을 삭제합니다. 등록된 사용자가 있으면 삭제 불가합니다.")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("독서과정이 삭제되었습니다."));
    }
}
