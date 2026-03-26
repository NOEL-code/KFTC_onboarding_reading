package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class UserBatchResponse {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<BatchResultItem> results;

    @Getter @Builder
    public static class BatchResultItem {
        private int index;
        private boolean success;
        private Long userId;
        private String message;
    }
}
