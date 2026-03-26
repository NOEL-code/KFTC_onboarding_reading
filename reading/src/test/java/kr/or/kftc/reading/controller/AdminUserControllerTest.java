package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.CourseUserListResponse;
import kr.or.kftc.reading.dto.CourseUserListResponse.CourseUserItem;
import kr.or.kftc.reading.dto.ExcelUploadResponse;
import kr.or.kftc.reading.dto.UserBatchResponse;
import kr.or.kftc.reading.dto.UserBatchResponse.BatchResultItem;
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
    @DisplayName("GET /api/admin/users?courseId=1 - 과정별 사용자 목록 조회")
    void getUsersByCourse() throws Exception {
        CourseUserListResponse response = CourseUserListResponse.builder()
                .courseId(1L).courseName("26년 상반기 독서과정").totalCount(2)
                .users(List.of(
                        CourseUserItem.builder().userId(1L).enrollmentId(1L)
                                .employeeNo("20260001").name("김민수").team("IT개발팀")
                                .enrolledAt("2026-01-15T09:00:00").build(),
                        CourseUserItem.builder().userId(2L).enrollmentId(2L)
                                .employeeNo("20260002").name("이영희").team("IT개발팀")
                                .enrolledAt("2026-01-15T09:00:00").build()))
                .build();

        when(userService.getUsersByCourse(1L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users")
                        .param("courseId", "1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.users[0].name").value("김민수"))
                .andExpect(jsonPath("$.users[1].employeeNo").value("20260002"));
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
                .andExpect(jsonPath("$.successCount").value(9));
    }

    @Test
    @DisplayName("POST /api/admin/users - 사용자 일괄 추가")
    void createUsers() throws Exception {
        UserBatchResponse response = UserBatchResponse.builder()
                .totalCount(2).successCount(2).failCount(0)
                .results(List.of(
                        BatchResultItem.builder().index(0).success(true).userId(1L).message("등록 성공").build(),
                        BatchResultItem.builder().index(1).success(true).userId(2L).message("등록 성공").build()))
                .build();

        when(userService.createUsers(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"users\":[{\"courseId\":1,\"employeeNo\":\"20210001\",\"name\":\"김민수\",\"team\":\"IT개발팀\"},{\"courseId\":1,\"employeeNo\":\"20210002\",\"name\":\"이영희\",\"team\":\"기획팀\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.results[0].userId").value(1));
    }

    @Test
    @DisplayName("PUT /api/admin/users - 사용자 일괄 수정")
    void updateUsers() throws Exception {
        UserBatchResponse response = UserBatchResponse.builder()
                .totalCount(2).successCount(2).failCount(0)
                .results(List.of(
                        BatchResultItem.builder().index(0).success(true).userId(1L).message("수정 성공").build(),
                        BatchResultItem.builder().index(1).success(true).userId(2L).message("수정 성공").build()))
                .build();

        when(userService.updateUsers(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/users")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1,\"users\":[{\"userId\":1,\"name\":\"김민수(수정)\"},{\"userId\":2,\"team\":\"경영지원팀\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(2));
    }

    @Test
    @DisplayName("DELETE /api/admin/users - 사용자 일괄 삭제")
    void deleteUsers() throws Exception {
        UserBatchResponse response = UserBatchResponse.builder()
                .totalCount(2).successCount(2).failCount(0)
                .results(List.of(
                        BatchResultItem.builder().index(0).success(true).userId(1L).message("삭제 성공").build(),
                        BatchResultItem.builder().index(1).success(true).userId(2L).message("삭제 성공").build()))
                .build();

        when(userService.deleteUsers(eq(1L), any())).thenReturn(response);

        mockMvc.perform(delete("/api/admin/users")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1,\"userIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(2));
    }

    @Test
    @DisplayName("POST /api/admin/users - 인증 없이 접근하면 403")
    void createUserUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"users\":[{\"courseId\":1,\"employeeNo\":\"20210001\"}]}"))
                .andExpect(status().isForbidden());
    }
}
