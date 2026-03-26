package kr.or.kftc.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.or.kftc.reading.dto.EmployeeResponse;
import kr.or.kftc.reading.entity.User;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 - 사원 조회", description = "사번으로 사원 정보 조회 API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserRepository userRepository;

    @Operation(summary = "사번으로 사원 조회", description = "사번을 입력하면 이름과 팀 정보를 반환합니다.")
    @GetMapping("/{employeeNo}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable String employeeNo) {
        User user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "등록되지 않은 사번입니다."));

        return ResponseEntity.ok(EmployeeResponse.builder()
                .userId(user.getId())
                .employeeNo(user.getEmployeeNo())
                .name(user.getName())
                .team(user.getTeam())
                .build());
    }
}
