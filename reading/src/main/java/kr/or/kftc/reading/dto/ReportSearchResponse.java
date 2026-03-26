package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportSearchResponse {
    private List<SearchResult> results;

    @Getter
    @Builder
    public static class SearchResult {
        private Long reportId;
        private String courseName;
        private String name;
        private String team;
        private String title;
        private String status;
        private String submittedAt;
    }
}
