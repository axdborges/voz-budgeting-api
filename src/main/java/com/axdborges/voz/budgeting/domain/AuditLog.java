package com.axdborges.voz.budgeting.domain;

import java.time.LocalDateTime;

public record AuditLog(Long id, TransactionId transactionId, AuditAction action, String detail,
                        LocalDateTime occurredAt) {

    public AuditLog(TransactionId transactionId, AuditAction action, String detail, LocalDateTime occurredAt) {
        this(null, transactionId, action, detail, occurredAt);
    }
}
