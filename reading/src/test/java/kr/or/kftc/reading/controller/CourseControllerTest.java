package kr.or.kftc.reading.controller;

import kr.or.kftc.reading.config.SecurityConfig;
import kr.or.kftc.reading.dto.CourseListResponse;
import kr.or.kftc.reading.security.JwtAuthenticationFilter;
import kr.or.kftc.reading.security.JwtUtil;
import kr.or.kftc.reading.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CourseControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CourseService courseService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/courses - 독서과정 목록 조회")
    void getCourses() throws Exception {
        CourseListResponse response = CourseListResponse.builder()
                .courses(List.of(
                        CourseListResponse.CourseItem.builder()
                                .courseId(1L).name("26년 상반기").status("진행중").build(),
                        CourseListResponse.CourseItem.builder()
                                .courseId(2L).name("25년 하반기").status("완료").build()))
                .build();

        when(courseService.getAllCourses()).thenReturn(response);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.courses[0].name").value("26년 상반기"));
    }
}
