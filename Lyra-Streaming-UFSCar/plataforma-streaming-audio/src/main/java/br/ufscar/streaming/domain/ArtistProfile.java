package br.ufscar.streaming.domain;

import jakarta.persistence.*;

/**
 * Entidade de domínio que representa o perfil público de um Artista ou Canal de Podcast.
 * Atende aos requisitos RF009, RF011, RF018 e ao caso de uso UC07.
 */
@Entity
@Table(name = "artist_profiles")
public class ArtistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String artisticName;

    @Column(length = 1200)
    private String biography;

    @Column(length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private String genres;

    private Long ownerUserId;

    private int followersCount = 0;

    public ArtistProfile() {}

    public ArtistProfile(String artisticName, String biography, String photoUrl,
                         String genres, Long ownerUserId, int followersCount) {
        this.artisticName = artisticName;
        this.biography = biography;
        this.photoUrl = photoUrl;
        this.genres = genres;
        this.ownerUserId = ownerUserId;
        this.followersCount = followersCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getArtisticName() { return artisticName; }
    public void setArtisticName(String artisticName) { this.artisticName = artisticName; }
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
}
