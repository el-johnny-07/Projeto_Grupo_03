package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório JPA para operações de persistência do histórico de reprodução {@link PlayHistory}.
 */
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId);
}
