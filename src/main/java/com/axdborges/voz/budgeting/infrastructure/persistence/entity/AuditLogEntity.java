package com.axdborges.voz.budgeting.infrastructure.persistence.entity;

import com.axdborges.voz.budgeting.domain.AuditAction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    private String detail;

    private LocalDateTime occurredAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(String transactionId, AuditAction action, String detail, LocalDateTime occurredAt) {
        this.transactionId = transactionId;
        this.action = action;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
