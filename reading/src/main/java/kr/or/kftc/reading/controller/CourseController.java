package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.CourseListResponse;
import kr.or.kftc.reading.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 - 독서과정", description = "독서과정 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "독서과정 목록 조회", description = "진행중인 독서과정 목록을 조회합니다. (드롭다운용)")
    @GetMapping("/courses")
    public ResponseEntity<CourseListResponse> getCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
}
