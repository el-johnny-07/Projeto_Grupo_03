package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.AbuseReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório JPA para operações de persistência de denúncias {@link AbuseReport}.
 */
public interface AbuseReportRepository extends JpaRepository<AbuseReport, Long> {
    List<AbuseReport> findAllByOrderByCreatedAtDesc();
}
