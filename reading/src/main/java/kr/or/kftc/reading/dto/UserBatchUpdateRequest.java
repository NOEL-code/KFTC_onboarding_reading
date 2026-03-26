package kr.or.kftc.reading.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UserBatchUpdateRequest {
    private List<UserUpdateRequest> users;
}
