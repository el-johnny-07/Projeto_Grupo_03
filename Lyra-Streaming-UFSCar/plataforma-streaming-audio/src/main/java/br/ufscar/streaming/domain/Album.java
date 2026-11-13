package br.ufscar.streaming.domain;

import jakarta.persistence.*;

/**
 * Entidade de domínio que representa um Álbum, EP, Single ou Programa de Podcast.
 * Atende aos requisitos RF007, RF010, RF011 e ao caso de uso UC07.
 */
@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseModality modality = ReleaseModality.ALBUM;

    @Column(nullable = false)
    private Long artistId;

    @Column(nullable = false)
    private String artistName;

    private int releaseYear;

    private String releaseDate;

    @Column(length = 800)
    private String description;

    @Column(length = 500)
    private String coverUrl;

    private String participatingArtists;

    public Album() {}

    public Album(String title, ReleaseModality modality, Long artistId, String artistName,
                 int releaseYear, String releaseDate, String description, String coverUrl, String participatingArtists) {
        this.title = title;
        this.modality = modality;
        this.artistId = artistId;
        this.artistName = artistName;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.description = description;
        this.coverUrl = coverUrl;
        this.participatingArtists = participatingArtists;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public ReleaseModality getModality() { return modality; }
    public void setModality(ReleaseModality modality) { this.modality = modality; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getParticipatingArtists() { return participatingArtists; }
    public void setParticipatingArtists(String participatingArtists) { this.participatingArtists = participatingArtists; }
}
