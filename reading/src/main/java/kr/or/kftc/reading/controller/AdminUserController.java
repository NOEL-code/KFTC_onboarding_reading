package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.service.ExcelService;
import kr.or.kftc.reading.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final ExcelService excelService;

    // API 10: POST /api/admin/users/upload — 엑셀 업로드
    @PostMapping("/upload")
    public ResponseEntity<ExcelUploadResponse> uploadExcel(
            @RequestParam Long courseId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelService.uploadExcel(courseId, file));
    }

    // API 11: POST /api/admin/users — 사용자 개별 추가
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    // API 12: PUT /api/admin/users/{userId} — 사용자 수정
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    // API 13: DELETE /api/admin/users/{userId} — 사용자 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long userId,
            @RequestParam Long courseId) {
        userService.deleteUser(userId, courseId);
        return ResponseEntity.ok(new MessageResponse("사용자가 삭제되었습니다."));
    }
}
