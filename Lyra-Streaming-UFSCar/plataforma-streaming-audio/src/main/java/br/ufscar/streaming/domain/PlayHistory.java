package br.ufscar.streaming.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que registra eventos cronológicos de audição e validação de 30s.
 * Atende aos requisitos RF015, RF022, RF023 e aos casos de uso UC02 e UC03.
 */
@Entity
@Table(name = "play_history")
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long trackId;

    private String trackTitle;
    private String trackAuthors;

    private int listenedSeconds;
    private boolean validatedOver30s;

    private LocalDateTime playedAt = LocalDateTime.now();

    public PlayHistory() {}

    public PlayHistory(Long userId, Long trackId, String trackTitle,
                       String trackAuthors, int listenedSeconds, boolean validatedOver30s) {
        this.userId = userId;
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.trackAuthors = trackAuthors;
        this.listenedSeconds = listenedSeconds;
        this.validatedOver30s = validatedOver30s;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }
    public String getTrackTitle() { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
    public String getTrackAuthors() { return trackAuthors; }
    public void setTrackAuthors(String trackAuthors) { this.trackAuthors = trackAuthors; }
    public int getListenedSeconds() { return listenedSeconds; }
    public void setListenedSeconds(int listenedSeconds) { this.listenedSeconds = listenedSeconds; }
    public boolean isValidatedOver30s() { return validatedOver30s; }
    public void setValidatedOver30s(boolean validatedOver30s) { this.validatedOver30s = validatedOver30s; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
