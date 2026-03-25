package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExcelUploadResponse {
    private int totalRows;
    private int successCount;
    private int failCount;
    private List<RowError> errors;

    @Getter
    @Builder
    public static class RowError {
        private int row;
        private String reason;
    }
}
