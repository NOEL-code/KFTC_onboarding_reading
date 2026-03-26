package kr.or.kftc.reading.config;

import kr.or.kftc.reading.entity.Admin;
import kr.or.kftc.reading.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = Admin.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("admin"))
                    .name("관리자1")
                    .build();
            adminRepository.save(admin);
        }
    }
}
