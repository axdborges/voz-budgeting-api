package com.axdborges.voz.budgeting.infrastructure.http.response;

import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String transactionId, AuditAction action, String detail,
                                LocalDateTime occurredAt) {

    public static AuditLogResponse from(AuditLogOutput output) {
        return new AuditLogResponse(output.id(), output.transactionId(), output.action(), output.detail(),
                output.occurredAt());
    }
}
