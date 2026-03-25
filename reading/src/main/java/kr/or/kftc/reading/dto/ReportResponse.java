package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long reportId;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
