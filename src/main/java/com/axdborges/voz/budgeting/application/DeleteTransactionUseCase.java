package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository,
                                     AuditLogRepository auditLogRepository) {
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public void execute(TransactionId id) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        transactionRepository.deleteById(id);

        String detail = "Categoria: %s, valor: R$ %s%s".formatted(existing.category(), existing.amount(),
                existing.description() != null ? ", descrição: " + existing.description() : "");
        auditLogRepository.save(new AuditLog(existing.id(), AuditAction.DELETED, detail, LocalDateTime.now()));
    }
}
