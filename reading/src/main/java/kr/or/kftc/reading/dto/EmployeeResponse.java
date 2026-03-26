package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeResponse {
    private Long userId;
    private String employeeNo;
    private String name;
    private String team;
}
