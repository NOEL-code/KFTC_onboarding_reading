package kr.or.kftc.reading.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CourseStatusUpdateRequest {
    @NotNull(message = "상태값은 필수입니다.")
    private String status;
}
