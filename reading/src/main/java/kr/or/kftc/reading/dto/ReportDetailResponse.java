package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportDetailResponse {
    private Long reportId;
    private String courseName;
    private String employeeNo;
    private String name;
    private String team;
    private String title;
    private String fileName;
    private Long fileSize;
    private String status;
    private String supplementReason;
    private LocalDateTime submittedAt;
}
