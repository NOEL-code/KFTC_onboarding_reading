package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.UserCreateRequest;
import kr.or.kftc.reading.dto.UserUpdateRequest;
import kr.or.kftc.reading.entity.CourseEnrollment;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.entity.User;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import kr.or.kftc.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReadingCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    @Transactional
    public Map<String, Object> createUser(UserCreateRequest request) {
        ReadingCourse course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        User user = userRepository.findByEmployeeNo(request.getEmployeeNo())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .employeeNo(request.getEmployeeNo())
                            .name(request.getName())
                            .department(request.getDepartment())
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .build();
                    return userRepository.save(newUser);
                });

        if (enrollmentRepository.existsByCourseIdAndUserId(course.getId(), user.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 해당 과정에 등록된 사용자입니다.");
        }

        CourseEnrollment enrollment = enrollmentRepository.save(
                CourseEnrollment.builder()
                        .course(course)
                        .user(user)
                        .build());

        return Map.of("userId", user.getId(), "enrollmentId", enrollment.getId());
    }

    @Transactional
    public Map<String, Object> updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        return Map.of("userId", user.getId(), "message", "사용자 정보가 수정되었습니다.");
    }

    @Transactional
    public void deleteUser(Long userId, Long courseId) {
        if (!enrollmentRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "해당 과정에 등록된 사용자를 찾을 수 없습니다.");
        }
        enrollmentRepository.deleteByCourseIdAndUserId(courseId, userId);
    }
}
