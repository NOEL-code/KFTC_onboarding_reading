package kr.or.kftc.reading.config;

import kr.or.kftc.reading.entity.*;
import kr.or.kftc.reading.repository.*;
import static kr.or.kftc.reading.entity.CourseStatus.*;
import static kr.or.kftc.reading.entity.ReportStatus.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ReadingCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final BookReportRepository reportRepository;
    private final ReportTemplateRepository templateRepository;
    private final NoticeRepository noticeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            log.info("데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("===== 테스트 데이터 초기화 시작 =====");

        // ── 1. 관리자 2명 ──
        Admin admin1 = adminRepository.save(Admin.builder()
                .loginId("admin").password(passwordEncoder.encode("admin")).name("관리자1").build());
        Admin admin2 = adminRepository.save(Admin.builder()
                .loginId("admin02").password(passwordEncoder.encode("admin1234")).name("관리자2").build());
        log.info("관리자 2명 생성 완료 (admin/admin, admin02/admin1234)");

        // ── 2. 독서과정 3개 (진행중 / 진행중 / 종료) ──
        ReadingCourse course1 = courseRepository.save(ReadingCourse.builder()
                .name("26년 상반기 독서과정")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .description("2026년 상반기 신입직원 독서과정입니다. 자기개발 도서 1권을 읽고 독후감을 제출해 주세요.")
                .status(진행중).build());

        ReadingCourse course2 = courseRepository.save(ReadingCourse.builder()
                .name("26년 하반기 독서과정")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .description("2026년 하반기 독서과정입니다.")
                .status(진행중).build());

        ReadingCourse course3 = courseRepository.save(ReadingCourse.builder()
                .name("25년 하반기 독서과정")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .description("2025년 하반기 독서과정 (종료)")
                .status(종료).build());

        log.info("독서과정 3개 생성 완료");

        // ── 3. 사용자 15명 (다양한 부서) ──
        User u1  = saveUser("20260001", "김민수", "IT개발팀");
        User u2  = saveUser("20260002", "이영희", "IT개발팀");
        User u3  = saveUser("20260003", "박준혁", "IT개발팀");
        User u4  = saveUser("20260004", "최수진", "경영지원팀");
        User u5  = saveUser("20260005", "정다은", "경영지원팀");
        User u6  = saveUser("20260006", "한지민", "기획팀");
        User u7  = saveUser("20260007", "오세훈", "기획팀");
        User u8  = saveUser("20260008", "윤서연", "금융결제팀");
        User u9  = saveUser("20260009", "장원영", "금융결제팀");
        User u10 = saveUser("20260010", "임재현", "금융결제팀");
        User u11 = saveUser("20260011", "송민호", "리스크관리팀");
        User u12 = saveUser("20260012", "강하늘", "리스크관리팀");
        User u13 = saveUser("20260013", "조은서", "디지털혁신팀");
        User u14 = saveUser("20260014", "신동욱", "디지털혁신팀");
        User u15 = saveUser("20260015", "배수지", "인사팀");

        log.info("사용자 15명 생성 완료");

        // ── 4. 과정 등록 (course1에 12명, course2에 5명, course3에 3명) ──
        // 과정1 (26년 상반기) - 12명
        CourseEnrollment e1  = enroll(course1, u1);
        CourseEnrollment e2  = enroll(course1, u2);
        CourseEnrollment e3  = enroll(course1, u3);
        CourseEnrollment e4  = enroll(course1, u4);
        CourseEnrollment e5  = enroll(course1, u5);
        CourseEnrollment e6  = enroll(course1, u6);
        CourseEnrollment e7  = enroll(course1, u7);
        CourseEnrollment e8  = enroll(course1, u8);
        CourseEnrollment e9  = enroll(course1, u9);
        CourseEnrollment e10 = enroll(course1, u10);
        CourseEnrollment e11 = enroll(course1, u11);
        CourseEnrollment e12 = enroll(course1, u12);

        // 과정2 (26년 하반기) - 5명
        CourseEnrollment e13 = enroll(course2, u13);
        CourseEnrollment e14 = enroll(course2, u14);
        CourseEnrollment e15 = enroll(course2, u15);
        CourseEnrollment e16 = enroll(course2, u1);  // 김민수 - 두 과정에 등록
        CourseEnrollment e17 = enroll(course2, u6);  // 한지민 - 두 과정에 등록

        // 과정3 (25년 하반기, 종료) - 3명
        CourseEnrollment e18 = enroll(course3, u1);
        CourseEnrollment e19 = enroll(course3, u2);
        CourseEnrollment e20 = enroll(course3, u4);

        log.info("과정 등록 20건 생성 완료");

        // ── 5. 독후감 (다양한 상태: 제출 / 승인 / 보완 / 미제출) ──
        byte[] dummyHwp = "HWP dummy file content for testing".getBytes();

        // 과정1 독후감 - 다양한 상태
        // 제출 상태 (4건)
        saveReport(e1, "리더십의 조건 - 독후감", "김민수_리더십의조건.hwp", dummyHwp, 제출,
                LocalDateTime.of(2026, 2, 15, 9, 30));
        saveReport(e2, "경제학 콘서트를 읽고", "이영희_경제학콘서트.hwp", dummyHwp, 제출,
                LocalDateTime.of(2026, 2, 20, 14, 0));
        saveReport(e3, "프로그래머의 길, 멘토에게 묻다", "박준혁_프로그래머의길.hwp", dummyHwp, 제출,
                LocalDateTime.of(2026, 3, 1, 10, 15));
        saveReport(e8, "디지털 금융의 미래", "윤서연_디지털금융의미래.hwp", dummyHwp, 제출,
                LocalDateTime.of(2026, 3, 10, 16, 45));

        // 승인 상태 (3건)
        saveReport(e4, "일의 격 - 독후감", "최수진_일의격.hwp", dummyHwp, 승인,
                LocalDateTime.of(2026, 1, 25, 11, 0));
        saveReport(e5, "데일 카네기 인간관계론", "정다은_인간관계론.hwp", dummyHwp, 승인,
                LocalDateTime.of(2026, 2, 5, 9, 0));
        saveReport(e6, "사피엔스를 읽고", "한지민_사피엔스.hwp", dummyHwp, 승인,
                LocalDateTime.of(2026, 2, 10, 13, 30));

        // 보완 상태 (2건)
        saveReport(e7, "독후감 제목 미정", "오세훈_독후감.hwp", dummyHwp, 보완,
                LocalDateTime.of(2026, 2, 28, 17, 0));
        saveReport(e9, "금융과 기술의 융합에 대하여", "장원영_금융기술.hwp", dummyHwp, 보완,
                LocalDateTime.of(2026, 3, 5, 11, 30));

        // 미제출 상태 (3건 - 파일 없음)
        saveReport(e10, null, null, null, 미제출, null);
        saveReport(e11, null, null, null, 미제출, null);
        saveReport(e12, null, null, null, 미제출, null);

        // 과정3 (종료된 과정) 독후감 - 모두 승인
        saveReport(e18, "작년에 읽은 책 독후감", "김민수_작년독후감.hwp", dummyHwp, 승인,
                LocalDateTime.of(2025, 10, 15, 9, 0));
        saveReport(e19, "2025 하반기 독후감", "이영희_2025독후감.hwp", dummyHwp, 승인,
                LocalDateTime.of(2025, 11, 1, 14, 0));
        saveReport(e20, "25년 독서 후기", "최수진_25년독서후기.hwp", dummyHwp, 승인,
                LocalDateTime.of(2025, 11, 20, 10, 0));

        log.info("독후감 15건 생성 완료 (제출:4, 승인:6, 보완:2, 미제출:3)");

        // ── 6. 독후감 양식 템플릿 (사용중 1개 + 미사용 1개) ──
        byte[] templateData = "HWP template dummy content".getBytes();

        templateRepository.save(ReportTemplate.builder()
                .fileName("KFTC_독후감_양식_v1.hwp")
                .fileData(templateData).fileSize((long) templateData.length)
                .uploadedBy(admin1).status("미사용")
                .uploadedAt(LocalDateTime.of(2025, 12, 1, 9, 0)).build());

        templateRepository.save(ReportTemplate.builder()
                .fileName("KFTC_독후감_양식_v2.hwp")
                .fileData(templateData).fileSize((long) templateData.length)
                .uploadedBy(admin1).status("사용중")
                .uploadedAt(LocalDateTime.of(2026, 1, 5, 9, 0)).build());

        log.info("양식 템플릿 2건 생성 완료 (v1:미사용, v2:사용중)");

        // ── 7. 공지사항 3건 ──
        noticeRepository.save(Notice.builder()
                .title("2026년 상반기 독서과정 안내")
                .content("안녕하세요. 2026년 상반기 독서과정이 시작되었습니다.\n\n"
                        + "- 기간: 2026.01.01 ~ 2026.06.30\n"
                        + "- 대상: 신입직원 전원\n"
                        + "- 제출방법: 독후감 양식을 다운로드하여 작성 후 HWP 파일로 업로드\n"
                        + "- 문의: 인사팀 배수지 (내선 7777)")
                .createdBy(admin1).build());

        noticeRepository.save(Notice.builder()
                .title("독후감 양식 변경 안내 (v2)")
                .content("독후감 양식이 v2로 업데이트되었습니다.\n"
                        + "기존 v1 양식으로 제출한 분은 재제출하실 필요 없습니다.\n"
                        + "새로 제출하시는 분은 v2 양식을 사용해 주세요.")
                .createdBy(admin1).build());

        noticeRepository.save(Notice.builder()
                .title("독후감 제출 마감 1개월 전 알림")
                .content("독후감 제출 마감이 1개월 남았습니다.\n"
                        + "아직 미제출 상태인 분들은 서둘러 제출해 주세요.\n\n"
                        + "보완 요청을 받으신 분은 수정 후 재제출 부탁드립니다.")
                .createdBy(admin2).build());

        log.info("공지사항 3건 생성 완료");

        log.info("===== 테스트 데이터 초기화 완료 =====");
        log.info("  관리자: admin/admin, admin02/admin1234");
        log.info("  독서과정: 3개 (진행중 2개, 종료 1개)");
        log.info("  사용자: 15명 (6개 팀)");
        log.info("  과정등록: 20건");
        log.info("  독후감: 15건 (제출4, 승인6, 보완2, 미제출3)");
        log.info("  양식: 2건 (사용중 1개)");
        log.info("  공지: 3건");
    }

    private User saveUser(String employeeNo, String name, String team) {
        return userRepository.save(User.builder()
                .employeeNo(employeeNo).name(name).team(team).build());
    }

    private CourseEnrollment enroll(ReadingCourse course, User user) {
        return enrollmentRepository.save(CourseEnrollment.builder()
                .course(course).user(user).build());
    }

    private void saveReport(CourseEnrollment enrollment, String title, String fileName,
                            byte[] fileData, ReportStatus status, LocalDateTime submittedAt) {
        reportRepository.save(BookReport.builder()
                .enrollment(enrollment).title(title).fileName(fileName)
                .fileData(fileData).fileSize(fileData != null ? (long) fileData.length : null)
                .status(status).submittedAt(submittedAt).build());
    }
}
