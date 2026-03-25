package kr.or.kftc.reading.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private String name;
    private long expiresIn;
}
