package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.UserBatchResponse;
import kr.or.kftc.reading.dto.UserBatchResponse.BatchResultItem;
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

import kr.or.kftc.reading.dto.CourseUserListResponse;
import kr.or.kftc.reading.dto.CourseUserListResponse.CourseUserItem;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReadingCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    public CourseUserListResponse getUsersByCourse(Long courseId) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        List<CourseUserItem> users = enrollments.stream()
                .map(e -> {
                    User u = e.getUser();
                    return CourseUserItem.builder()
                            .userId(u.getId())
                            .enrollmentId(e.getId())
                            .employeeNo(u.getEmployeeNo())
                            .name(u.getName())
                            .department(u.getDepartment())
                            .email(u.getEmail())
                            .phone(u.getPhone())
                            .enrolledAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null)
                            .build();
                })
                .toList();

        return CourseUserListResponse.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .totalCount(users.size())
                .users(users)
                .build();
    }

    @Transactional
    public UserBatchResponse createUsers(List<UserCreateRequest> requests) {
        List<BatchResultItem> results = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < requests.size(); i++) {
            UserCreateRequest req = requests.get(i);
            try {
                ReadingCourse course = courseRepository.findById(req.getCourseId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

                User user = userRepository.findByEmployeeNo(req.getEmployeeNo())
                        .orElseGet(() -> userRepository.save(User.builder()
                                .employeeNo(req.getEmployeeNo())
                                .name(req.getName())
                                .department(req.getDepartment())
                                .email(req.getEmail())
                                .phone(req.getPhone())
                                .build()));

                if (enrollmentRepository.existsByCourseIdAndUserId(course.getId(), user.getId())) {
                    results.add(BatchResultItem.builder()
                            .index(i).success(false).userId(user.getId())
                            .message("이미 해당 과정에 등록된 사용자입니다.").build());
                    continue;
                }

                enrollmentRepository.save(CourseEnrollment.builder().course(course).user(user).build());
                results.add(BatchResultItem.builder()
                        .index(i).success(true).userId(user.getId())
                        .message("등록 성공").build());
                success++;
            } catch (Exception e) {
                results.add(BatchResultItem.builder()
                        .index(i).success(false).userId(null)
                        .message(e.getMessage()).build());
            }
        }

        return UserBatchResponse.builder()
                .totalCount(requests.size()).successCount(success)
                .failCount(requests.size() - success).results(results).build();
    }

    @Transactional
    public UserBatchResponse updateUsers(Long courseId, List<UserUpdateRequest> requests) {
        List<BatchResultItem> results = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < requests.size(); i++) {
            UserUpdateRequest req = requests.get(i);
            try {
                if (!enrollmentRepository.existsByCourseIdAndUserId(courseId, req.getUserId())) {
                    results.add(BatchResultItem.builder()
                            .index(i).success(false).userId(req.getUserId())
                            .message("해당 과정에 등록된 사용자를 찾을 수 없습니다.").build());
                    continue;
                }

                User user = userRepository.findById(req.getUserId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

                if (req.getName() != null) user.setName(req.getName());
                if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
                if (req.getEmail() != null) user.setEmail(req.getEmail());
                if (req.getPhone() != null) user.setPhone(req.getPhone());

                results.add(BatchResultItem.builder()
                        .index(i).success(true).userId(user.getId())
                        .message("수정 성공").build());
                success++;
            } catch (Exception e) {
                results.add(BatchResultItem.builder()
                        .index(i).success(false).userId(req.getUserId())
                        .message(e.getMessage()).build());
            }
        }

        return UserBatchResponse.builder()
                .totalCount(requests.size()).successCount(success)
                .failCount(requests.size() - success).results(results).build();
    }

    @Transactional
    public UserBatchResponse deleteUsers(Long courseId, List<Long> userIds) {
        List<BatchResultItem> results = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            try {
                if (!enrollmentRepository.existsByCourseIdAndUserId(courseId, userId)) {
                    results.add(BatchResultItem.builder()
                            .index(i).success(false).userId(userId)
                            .message("해당 과정에 등록된 사용자를 찾을 수 없습니다.").build());
                    continue;
                }
                enrollmentRepository.deleteByCourseIdAndUserId(courseId, userId);
                results.add(BatchResultItem.builder()
                        .index(i).success(true).userId(userId)
                        .message("삭제 성공").build());
                success++;
            } catch (Exception e) {
                results.add(BatchResultItem.builder()
                        .index(i).success(false).userId(userId)
                        .message(e.getMessage()).build());
            }
        }

        return UserBatchResponse.builder()
                .totalCount(userIds.size()).successCount(success)
                .failCount(userIds.size() - success).results(results).build();
    }
}
