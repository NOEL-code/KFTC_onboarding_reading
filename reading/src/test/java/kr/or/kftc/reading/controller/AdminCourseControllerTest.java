package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.CourseCreateRequest;
import kr.or.kftc.reading.dto.CourseUpdateRequest;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

@WebMvcTest(AdminCourseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminCourseControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CourseService courseService;
    @MockitoBean private JwtUtil jwtUtil;

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/admin/courses - 인증 없이 접근하면 403")
    void createCourseUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"26년 상반기\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/courses - 독서과정 생성 성공")
    void createCourse() throws Exception {
        when(courseService.createCourse(any(CourseCreateRequest.class)))
                .thenReturn(Map.of("courseId", 1L, "status", "진행중"));

        mockMvc.perform(post("/api/admin/courses")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"26년 상반기\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.status").value("진행중"));
    }

    @Test
    @DisplayName("PUT /api/admin/courses/{courseId} - 독서과정 수정")
    void updateCourse() throws Exception {
        when(courseService.updateCourse(eq(1L), any(CourseUpdateRequest.class)))
                .thenReturn(Map.of("courseId", 1L, "message", "독서과정이 수정되었습니다."));

        mockMvc.perform(put("/api/admin/courses/1")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"수정된 과정명\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("독서과정이 수정되었습니다."));
    }

    @Test
    @DisplayName("DELETE /api/admin/courses/{courseId} - 독서과정 삭제")
    void deleteCourse() throws Exception {
        mockMvc.perform(delete("/api/admin/courses/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("독서과정이 삭제되었습니다."));
    }
}
