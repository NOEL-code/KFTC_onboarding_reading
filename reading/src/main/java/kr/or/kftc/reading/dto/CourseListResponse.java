package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseListResponse {
    private List<CourseItem> courses;

    @Getter
    @Builder
    public static class CourseItem {
        private Long courseId;
        private String name;
        private String status;
        private String startDate;
        private String endDate;
        private long totalUsers;
        private long submittedUsers;
    }
}
