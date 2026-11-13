package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório JPA para operações de persistência da entidade {@link Playlist}.
 */
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerUserIdOrPublicVisibleTrue(Long ownerUserId);
}
