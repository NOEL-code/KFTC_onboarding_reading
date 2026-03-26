package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportListResponse {
    private long totalCount;
    private int page;
    private int size;
    private int totalPages;
    private List<ReportSummary> reports;

    @Getter
    @Builder
    public static class ReportSummary {
        private Long reportId;
        private String employeeNo;
        private String name;
        private String team;
        private String title;
        private String status;
        private String submittedAt;
    }
}
