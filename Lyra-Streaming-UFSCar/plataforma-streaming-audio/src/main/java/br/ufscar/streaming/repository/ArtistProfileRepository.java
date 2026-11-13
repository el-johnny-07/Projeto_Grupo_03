package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA para operações de persistência da entidade {@link ArtistProfile}.
 */
public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, Long> {
}
