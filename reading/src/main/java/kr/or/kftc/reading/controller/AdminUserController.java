package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.*;
import kr.or.kftc.reading.service.ExcelService;
import kr.or.kftc.reading.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "관리자 - 사용자 관리", description = "사용자 등록/수정/삭제 API (JWT 필요)")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final ExcelService excelService;

    @Operation(summary = "엑셀 일괄 업로드", description = "엑셀 파일로 사용자를 일괄 등록합니다.")
    @PostMapping("/upload")
    public ResponseEntity<ExcelUploadResponse> uploadExcel(
            @RequestParam Long courseId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelService.uploadExcel(courseId, file));
    }

    @Operation(summary = "사용자 개별 추가", description = "사용자를 개별적으로 추가하고 과정에 등록합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @Operation(summary = "사용자 수정", description = "사용자 정보를 수정합니다.")
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(summary = "사용자 삭제", description = "과정에서 사용자를 삭제합니다.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long userId,
            @RequestParam Long courseId) {
        userService.deleteUser(userId, courseId);
        return ResponseEntity.ok(new MessageResponse("사용자가 삭제되었습니다."));
    }
}
