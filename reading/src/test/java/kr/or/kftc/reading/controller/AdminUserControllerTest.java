package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.ExcelUploadResponse;
import kr.or.kftc.reading.dto.UserCreateRequest;
import kr.or.kftc.reading.dto.UserUpdateRequest;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.ExcelService;
import kr.or.kftc.reading.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminUserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private ExcelService excelService;
    @MockitoBean private JwtUtil jwtUtil;

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/admin/users/upload - 엑셀 업로드 성공")
    void uploadExcel() throws Exception {
        ExcelUploadResponse response = ExcelUploadResponse.builder()
                .totalRows(10).successCount(9).failCount(1)
                .errors(List.of(
                        ExcelUploadResponse.RowError.builder().row(5).reason("사번 누락").build()))
                .build();

        when(excelService.uploadExcel(eq(1L), any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "data".getBytes());

        mockMvc.perform(multipart("/api/admin/users/upload")
                        .file(file)
                        .param("courseId", "1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(10))
                .andExpect(jsonPath("$.successCount").value(9))
                .andExpect(jsonPath("$.failCount").value(1))
                .andExpect(jsonPath("$.errors[0].row").value(5));
    }

    @Test
    @DisplayName("POST /api/admin/users - 사용자 개별 추가")
    void createUser() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(Map.of("userId", 1L, "enrollmentId", 1L));

        mockMvc.perform(post("/api/admin/users")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1,\"employeeNo\":\"20210001\",\"name\":\"김민수\",\"department\":\"IT개발부\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{userId} - 사용자 수정")
    void updateUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                .thenReturn(Map.of("userId", 1L, "message", "사용자 정보가 수정되었습니다."));

        mockMvc.perform(put("/api/admin/users/1")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"김민수(수정)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 정보가 수정되었습니다."));
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{userId} - 사용자 삭제")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .param("courseId", "1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자가 삭제되었습니다."));
    }

    @Test
    @DisplayName("POST /api/admin/users - 인증 없이 접근하면 403")
    void createUserUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1,\"employeeNo\":\"20210001\"}"))
                .andExpect(status().isForbidden());
    }
}
