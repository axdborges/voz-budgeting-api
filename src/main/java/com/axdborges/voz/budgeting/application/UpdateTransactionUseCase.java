package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.UpdateTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
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

    public UpdateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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

        return new TransactionOutput(updated.id().value().toString(), updated.description(), updated.category(),
                updated.amount(), updated.occurredAt(), updated.updatedAt());
    }
}
