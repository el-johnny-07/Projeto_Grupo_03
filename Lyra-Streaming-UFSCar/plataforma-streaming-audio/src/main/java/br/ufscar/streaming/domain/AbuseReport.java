package br.ufscar.streaming.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa uma denúncia de violação de direitos autorais ou conduta.
 * Atende aos requisitos RF024, RF025 e ao caso de uso UC04.
 */
@Entity
@Table(name = "abuse_reports")
public class AbuseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long trackId;

    private String trackTitle;
    private String creatorName;

    private Long reporterUserId;
    private String reporterName;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDENTE;

    @Column(length = 1000)
    private String technicalOpinion;

    private String moderatorName;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public AbuseReport() {}

    public AbuseReport(Long trackId, String trackTitle, String creatorName, Long reporterUserId,
                       String reporterName, String reason, String description) {
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.creatorName = creatorName;
        this.reporterUserId = reporterUserId;
        this.reporterName = reporterName;
        this.reason = reason;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }
    public String getTrackTitle() { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getTechnicalOpinion() { return technicalOpinion; }
    public void setTechnicalOpinion(String technicalOpinion) { this.technicalOpinion = technicalOpinion; }
    public String getModeratorName() { return moderatorName; }
    public void setModeratorName(String moderatorName) { this.moderatorName = moderatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
