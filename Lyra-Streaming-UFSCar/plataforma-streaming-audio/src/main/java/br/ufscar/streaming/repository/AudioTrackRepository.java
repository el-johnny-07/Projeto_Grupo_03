package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.AudioTrack;
import br.ufscar.streaming.domain.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Repositório JPA para operações de persistência e consulta indexada de {@link AudioTrack}.
 */
public interface AudioTrackRepository extends JpaRepository<AudioTrack, Long> {
    List<AudioTrack> findByStatusOrderByPlaysLast24hDesc(MediaStatus status);
    List<AudioTrack> findByStatusOrderByPublishedAtDesc(MediaStatus status);
    List<AudioTrack> findByAudioFileKeyIsNotNullAndStatusOrderByPublishedAtDesc(MediaStatus status);
    @Modifying
    @Transactional
    @Query("update AudioTrack t set t.title = :title where t.id = :trackId and t.creatorUserId = :ownerId and t.audioFileKey is not null and t.status = :status")
    int renameUploadedTrack(Long trackId, Long ownerId, String title, MediaStatus status);

    List<AudioTrack> findByAlbumIdOrderByTrackOrderAsc(Long albumId);
    List<AudioTrack> findByArtistId(Long artistId);
}
