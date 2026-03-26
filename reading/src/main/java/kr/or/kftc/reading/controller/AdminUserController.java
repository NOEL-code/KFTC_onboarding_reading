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

@Tag(name = "관리자 - 사용자 관리", description = "사용자 일괄 등록/수정/삭제 API (JWT 필요)")
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

    @Operation(summary = "사용자 일괄 추가", description = "여러 사용자를 한 번에 추가하고 과정에 등록합니다.")
    @PostMapping
    public ResponseEntity<UserBatchResponse> createUsers(@RequestBody UserBatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUsers(request.getUsers()));
    }

    @Operation(summary = "사용자 일괄 수정", description = "여러 사용자의 정보를 한 번에 수정합니다.")
    @PutMapping
    public ResponseEntity<UserBatchResponse> updateUsers(@RequestBody UserBatchUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUsers(request.getUsers()));
    }

    @Operation(summary = "사용자 일괄 삭제", description = "여러 사용자를 과정에서 한 번에 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<UserBatchResponse> deleteUsers(@RequestBody UserBatchDeleteRequest request) {
        return ResponseEntity.ok(userService.deleteUsers(request.getCourseId(), request.getUserIds()));
    }
}
