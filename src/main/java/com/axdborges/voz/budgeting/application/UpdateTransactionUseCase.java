package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.UpdateTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public UpdateTransactionUseCase(TransactionRepository transactionRepository,
                                     AuditLogRepository auditLogRepository) {
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public TransactionOutput execute(TransactionId id, UpdateTransactionInput input) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        String description = input.description() != null ? input.description() : existing.description();
        Category category = input.category() != null ? input.category() : existing.category();
        var amount = input.amount() != null ? input.amount() : existing.amount();
        LocalDateTime occurredAt = input.date() != null
                ? input.date().atTime(existing.occurredAt().toLocalTime())
                : existing.occurredAt();

        Transaction updated = new Transaction(existing.id(), description, category, amount, occurredAt,
                LocalDateTime.now());

        transactionRepository.save(updated);
        auditLogRepository.save(new AuditLog(updated.id(), AuditAction.UPDATED, changeDetail(existing, input),
                updated.updatedAt()));

        return new TransactionOutput(updated.id().value().toString(), updated.description(), updated.category(),
                updated.amount(), updated.occurredAt(), updated.updatedAt());
    }

    private String changeDetail(Transaction existing, UpdateTransactionInput input) {
        StringBuilder detail = new StringBuilder();
        if (input.description() != null && !input.description().equals(existing.description())) {
            detail.append("descrição: '%s' -> '%s'; ".formatted(existing.description(), input.description()));
        }
        if (input.category() != null && input.category() != existing.category()) {
            detail.append("categoria: %s -> %s; ".formatted(existing.category(), input.category()));
        }
        if (input.amount() != null && input.amount().compareTo(existing.amount()) != 0) {
            detail.append("valor: R$ %s -> R$ %s; ".formatted(existing.amount(), input.amount()));
        }
        if (input.date() != null && !input.date().equals(existing.occurredAt().toLocalDate())) {
            detail.append("data: %s -> %s; ".formatted(existing.occurredAt().toLocalDate(), input.date()));
        }
        return !detail.isEmpty() ? detail.toString().trim() : "Nenhum campo alterado";
    }
}
