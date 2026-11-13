package br.ufscar.streaming.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de domínio que representa uma Lista de Reprodução (Playlist) personalizada.
 * Atende aos requisitos RF019, RF020 e ao caso de uso UC03.
 */
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean publicVisible = true;

    @Column(nullable = false)
    private Long ownerUserId;

    private String ownerName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "playlist_tracks", joinColumns = @JoinColumn(name = "playlist_id"))
    @OrderColumn(name = "position_index")
    @Column(name = "track_id")
    private List<Long> trackIds = new ArrayList<>();

    public Playlist() {}

    public Playlist(String name, String description, boolean publicVisible,
                    Long ownerUserId, String ownerName, List<Long> trackIds) {
        this.name = name;
        this.description = description;
        this.publicVisible = publicVisible;
        this.ownerUserId = ownerUserId;
        this.ownerName = ownerName;
        this.trackIds = new ArrayList<>(trackIds);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPublicVisible() { return publicVisible; }
    public void setPublicVisible(boolean publicVisible) { this.publicVisible = publicVisible; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public List<Long> getTrackIds() { return trackIds; }
    public void setTrackIds(List<Long> trackIds) { this.trackIds = trackIds; }
}
