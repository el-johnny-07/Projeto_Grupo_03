package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório JPA para operações de persistência da entidade {@link Album}.
 */
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtistId(Long artistId);
}
