package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório JPA para operações de persistência do livro de auditoria {@link AuditLog}.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
}
