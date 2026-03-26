package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UserBatchDeleteRequest {
    private Long courseId;
    private List<Long> userIds;
}
