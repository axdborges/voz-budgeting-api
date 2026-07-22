package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PersistTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository,
                                      AuditLogRepository auditLogRepository) {
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public TransactionOutput execute(PersistTransactionInput input) {
        LocalDateTime occurredAt = input.date() != null
                ? input.date().atTime(LocalDateTime.now().toLocalTime())
                : LocalDateTime.now();

        Transaction transaction = new Transaction(TransactionId.generate(), input.description(), input.category(),
                input.amount(), occurredAt);

        transactionRepository.save(transaction);

        String detail = "Categoria: %s, valor: R$ %s%s".formatted(transaction.category(), transaction.amount(),
                transaction.description() != null ? ", descrição: " + transaction.description() : "");
        auditLogRepository.save(new AuditLog(transaction.id(), AuditAction.CREATED, detail, LocalDateTime.now()));

        return new TransactionOutput(transaction.id().value().toString(), transaction.description(),
                transaction.category(), transaction.amount(), transaction.occurredAt(), transaction.updatedAt());
    }
}
