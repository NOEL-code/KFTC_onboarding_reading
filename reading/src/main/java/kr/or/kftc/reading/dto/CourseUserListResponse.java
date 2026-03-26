package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class CourseUserListResponse {
    private Long courseId;
    private String courseName;
    private int totalCount;
    private List<CourseUserItem> users;

    @Getter @Builder
    public static class CourseUserItem {
        private Long userId;
        private Long enrollmentId;
        private String employeeNo;
        private String name;
        private String department;
        private String email;
        private String phone;
        private String enrolledAt;
    }
}
