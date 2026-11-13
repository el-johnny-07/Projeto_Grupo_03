package br.ufscar.streaming.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa uma Obra Sonora (Faixa Musical ou Episódio de Podcast).
 * Atende aos requisitos RF005 a RF008, RF012 a RF017 e RF023.
 */
@Entity
@Table(name = "audio_tracks")
public class AudioTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaCategory category = MediaCategory.MUSICA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseModality modality = ReleaseModality.SINGLE;

    @Column(nullable = false)
    private String authors;

    private Long artistId;

    private Long albumId;

    private String albumTitle;

    @Column(nullable = false)
    private String genre;

    private int releaseYear;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String tags;

    @Column(nullable = false)
    private String audioFormat;

    private int bitrateKbps;

    private int coverWidth;
    private int coverHeight;

    @Column(length = 500)
    private String coverUrl;

    private int durationSeconds;

    @Column(length = 36)
    private String audioFileKey;

    private int trackOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status = MediaStatus.ATIVO;

    private long playsTotal = 0;
    private long playsLast24h = 0;
    private long uniqueListeners = 0;
    private double avgRetentionPercent = 0.0;

    private Long creatorUserId;

    private LocalDateTime publishedAt = LocalDateTime.now();

    public AudioTrack() {}

    public AudioTrack(String title, MediaCategory category, ReleaseModality modality, String authors,
                      Long artistId, Long albumId, String albumTitle, String genre, int releaseYear,
                      String description, String tags, String audioFormat, int bitrateKbps,
                      int coverWidth, int coverHeight, String coverUrl, int durationSeconds,
                      int trackOrder, long playsTotal, long playsLast24h, long uniqueListeners,
                      double avgRetentionPercent, Long creatorUserId) {
        this.title = title;
        this.category = category;
        this.modality = modality;
        this.authors = authors;
        this.artistId = artistId;
        this.albumId = albumId;
        this.albumTitle = albumTitle;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.description = description;
        this.tags = tags;
        this.audioFormat = audioFormat;
        this.bitrateKbps = bitrateKbps;
        this.coverWidth = coverWidth;
        this.coverHeight = coverHeight;
        this.coverUrl = coverUrl;
        this.durationSeconds = durationSeconds;
        this.trackOrder = trackOrder;
        this.playsTotal = playsTotal;
        this.playsLast24h = playsLast24h;
        this.uniqueListeners = uniqueListeners;
        this.avgRetentionPercent = avgRetentionPercent;
        this.creatorUserId = creatorUserId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public MediaCategory getCategory() { return category; }
    public void setCategory(MediaCategory category) { this.category = category; }
    public ReleaseModality getModality() { return modality; }
    public void setModality(ReleaseModality modality) { this.modality = modality; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }
    public int getBitrateKbps() { return bitrateKbps; }
    public void setBitrateKbps(int bitrateKbps) { this.bitrateKbps = bitrateKbps; }
    public int getCoverWidth() { return coverWidth; }
    public void setCoverWidth(int coverWidth) { this.coverWidth = coverWidth; }
    public int getCoverHeight() { return coverHeight; }
    public void setCoverHeight(int coverHeight) { this.coverHeight = coverHeight; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    @JsonIgnore
    public String getAudioFileKey() { return audioFileKey; }
    public void setAudioFileKey(String audioFileKey) { this.audioFileKey = audioFileKey; }
    public int getTrackOrder() { return trackOrder; }
    public void setTrackOrder(int trackOrder) { this.trackOrder = trackOrder; }
    public MediaStatus getStatus() { return status; }
    public void setStatus(MediaStatus status) { this.status = status; }
    public long getPlaysTotal() { return playsTotal; }
    public void setPlaysTotal(long playsTotal) { this.playsTotal = playsTotal; }
    public long getPlaysLast24h() { return playsLast24h; }
    public void setPlaysLast24h(long playsLast24h) { this.playsLast24h = playsLast24h; }
    public long getUniqueListeners() { return uniqueListeners; }
    public void setUniqueListeners(long uniqueListeners) { this.uniqueListeners = uniqueListeners; }
    public double getAvgRetentionPercent() { return avgRetentionPercent; }
    public void setAvgRetentionPercent(double avgRetentionPercent) { this.avgRetentionPercent = avgRetentionPercent; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
