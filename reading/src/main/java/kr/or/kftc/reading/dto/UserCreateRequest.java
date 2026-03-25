package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateRequest {
    private Long courseId;
    private String employeeNo;
    private String name;
    private String department;
    private String email;
    private String phone;
}
