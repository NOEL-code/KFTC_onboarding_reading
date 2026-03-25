package kr.or.kftc.reading.repository;

import kr.or.kftc.reading.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    Optional<ReportTemplate> findByStatus(String status);
}
