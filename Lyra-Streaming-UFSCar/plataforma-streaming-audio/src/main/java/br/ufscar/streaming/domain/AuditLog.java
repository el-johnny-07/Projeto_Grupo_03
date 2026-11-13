package br.ufscar.streaming.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa um registro imutável no livro eletrônico de auditoria.
 * Atende aos casos de uso UC04 e UC09 e aos requisitos RF026, RF027 e RNF009.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorName;
    private String actorRole;
    private String action;
    private String target;

    @Column(length = 1000)
    private String details;

    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String actorName, String actorRole, String action, String target, String details) {
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.action = action;
        this.target = target;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
