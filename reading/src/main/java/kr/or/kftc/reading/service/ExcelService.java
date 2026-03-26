package kr.or.kftc.reading.service;

import kr.or.kftc.reading.dto.ExcelUploadResponse;
import kr.or.kftc.reading.entity.CourseEnrollment;
import kr.or.kftc.reading.entity.ReadingCourse;
import kr.or.kftc.reading.entity.User;
import kr.or.kftc.reading.exception.BusinessException;
import kr.or.kftc.reading.repository.CourseEnrollmentRepository;
import kr.or.kftc.reading.repository.ReadingCourseRepository;
import kr.or.kftc.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final UserRepository userRepository;
    private final ReadingCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    @Transactional
    public ExcelUploadResponse uploadExcel(Long courseId, MultipartFile file) {
        ReadingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "독서과정을 찾을 수 없습니다."));

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "엑셀 파일(.xlsx, .xls)만 업로드 가능합니다.");
        }

        List<ExcelUploadResponse.RowError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = fileName.endsWith(".xlsx")
                    ? new XSSFWorkbook(is)
                    : new HSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();
            if (lastRow > 500) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "최대 500명까지 업로드 가능합니다.");
            }

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalRows++;

                try {
                    String employeeNo = getCellString(row.getCell(0));
                    String name = getCellString(row.getCell(1));
                    String team = getCellString(row.getCell(2));

                    if (employeeNo == null || employeeNo.isBlank()) {
                        errors.add(ExcelUploadResponse.RowError.builder()
                                .row(i + 1).reason("사번 누락").build());
                        continue;
                    }
                    if (name == null || name.isBlank()) {
                        errors.add(ExcelUploadResponse.RowError.builder()
                                .row(i + 1).reason("이름 누락").build());
                        continue;
                    }
                    if (team == null || team.isBlank()) {
                        errors.add(ExcelUploadResponse.RowError.builder()
                                .row(i + 1).reason("팀 누락").build());
                        continue;
                    }

                    User user = userRepository.findByEmployeeNo(employeeNo)
                            .map(existing -> {
                                existing.setName(name);
                                existing.setTeam(team);
                                return existing;
                            })
                            .orElseGet(() -> userRepository.save(User.builder()
                                    .employeeNo(employeeNo)
                                    .name(name)
                                    .team(team)
                                    .build()));

                    if (!enrollmentRepository.existsByCourseIdAndUserId(course.getId(), user.getId())) {
                        enrollmentRepository.save(CourseEnrollment.builder()
                                .course(course)
                                .user(user)
                                .build());
                    }
                    successCount++;
                } catch (Exception e) {
                    errors.add(ExcelUploadResponse.RowError.builder()
                            .row(i + 1).reason("처리 중 오류: " + e.getMessage()).build());
                }
            }
            workbook.close();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "엑셀 파일 처리 중 오류가 발생했습니다.");
        }

        return ExcelUploadResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failCount(errors.size())
                .errors(errors)
                .build();
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
