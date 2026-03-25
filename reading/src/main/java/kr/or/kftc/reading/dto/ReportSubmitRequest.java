package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportSubmitRequest {
    private Long courseId;
    private String employeeNo;
    private String title;
}
