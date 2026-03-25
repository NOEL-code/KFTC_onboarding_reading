package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByLoginId(String loginId);
}
