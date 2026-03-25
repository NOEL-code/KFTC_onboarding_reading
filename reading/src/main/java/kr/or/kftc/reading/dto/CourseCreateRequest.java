package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CourseCreateRequest {
    private String name;
    private String startDate;
    private String endDate;
    private String description;
}
