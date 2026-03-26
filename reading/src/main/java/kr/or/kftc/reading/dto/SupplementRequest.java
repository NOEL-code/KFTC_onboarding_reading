package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SupplementRequest {
    private List<Long> reportIds;
    private String reason;
}
