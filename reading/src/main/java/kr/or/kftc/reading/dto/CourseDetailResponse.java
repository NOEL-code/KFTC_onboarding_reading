package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDetailResponse {
    private Long courseId;
    private String name;
    private String status;
    private String startDate;
    private String endDate;
    private String description;
}
