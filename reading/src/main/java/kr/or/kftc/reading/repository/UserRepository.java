package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmployeeNo(String employeeNo);
    boolean existsByEmployeeNo(String employeeNo);
}
