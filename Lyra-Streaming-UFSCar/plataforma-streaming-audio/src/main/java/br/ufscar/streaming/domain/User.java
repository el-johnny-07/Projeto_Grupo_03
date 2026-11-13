package br.ufscar.streaming.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade de domínio que representa um usuário cadastrado na plataforma.
 * Atende aos requisitos RF001 a RF004, RF018, RF021, RF026, RNF006 e RNF007.
 */
@Entity
@DynamicUpdate
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.OUVINTE;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(length = 600)
    private String bio;

    private String recoveryToken;
    private String profilePictureUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorite_tracks", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "track_id")
    private Set<Long> favoriteTrackIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_followed_artists", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "artist_id")
    private Set<Long> followedArtistIds = new HashSet<>();

    public User() {}

    /**
     * Constrói uma nova instância de usuário com credenciais criptografadas.
     *
     * @param fullName nome completo do usuário (RF001)
     * @param email endereço de e-mail exclusivo (RF001)
     * @param passwordHash hash criptográfico BCrypt da senha (RNF006)
     * @param role papel de acesso RBAC atribuído (RNF007)
     * @param bio descrição biográfica do perfil pessoal (RF004)
     */
    public User(String fullName, String email, String passwordHash, Role role, String bio) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    @JsonIgnore
    public String getRecoveryToken() { return recoveryToken; }
    public void setRecoveryToken(String recoveryToken) { this.recoveryToken = recoveryToken; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Set<Long> getFavoriteTrackIds() { return favoriteTrackIds; }
    public void setFavoriteTrackIds(Set<Long> favoriteTrackIds) { this.favoriteTrackIds = favoriteTrackIds; }
    public Set<Long> getFollowedArtistIds() { return followedArtistIds; }
    public void setFollowedArtistIds(Set<Long> followedArtistIds) { this.followedArtistIds = followedArtistIds; }
}
