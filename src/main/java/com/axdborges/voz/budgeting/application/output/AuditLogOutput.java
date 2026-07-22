package com.axdborges.voz.budgeting.application.output;

import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;

import java.time.LocalDateTime;

public record AuditLogOutput(Long id, String transactionId, AuditAction action, String detail,
                              LocalDateTime occurredAt) {

    public static AuditLogOutput from(AuditLog auditLog) {
        return new AuditLogOutput(auditLog.id(), auditLog.transactionId().value().toString(), auditLog.action(),
                auditLog.detail(), auditLog.occurredAt());
    }
}
