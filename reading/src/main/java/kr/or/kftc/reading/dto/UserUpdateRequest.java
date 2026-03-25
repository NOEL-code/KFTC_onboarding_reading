package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserUpdateRequest {
    private String name;
    private String department;
    private String email;
    private String phone;
}
