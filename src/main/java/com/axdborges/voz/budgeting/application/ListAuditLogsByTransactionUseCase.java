package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.TransactionId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAuditLogsByTransactionUseCase {

    private final AuditLogRepository auditLogRepository;

    public ListAuditLogsByTransactionUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogOutput> execute(TransactionId transactionId) {
        return auditLogRepository.findByTransactionId(transactionId).stream().map(AuditLogOutput::from).toList();
    }
}
