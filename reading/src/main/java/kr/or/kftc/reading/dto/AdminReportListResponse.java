package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminReportListResponse {
    private Summary summary;
    private int page;
    private int size;
    private int totalPages;
    private List<AdminReportItem> reports;

    @Getter
    @Builder
    public static class Summary {
        private long total;
        private long submitted;
        private long approved;
        private long supplement;
        private long notSubmitted;
    }

    @Getter
    @Builder
    public static class AdminReportItem {
        private Long reportId;
        private Long enrollmentId;
        private String name;
        private String team;
        private String title;
        private String status;
        private String supplementReason;
        private String submittedAt;
    }
}
