package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PersistTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionOutput execute(PersistTransactionInput input) {
        LocalDateTime occurredAt = input.date() != null
                ? input.date().atTime(LocalDateTime.now().toLocalTime())
                : LocalDateTime.now();

        Transaction transaction = new Transaction(TransactionId.generate(), input.description(), input.category(),
                input.amount(), occurredAt);

        transactionRepository.save(transaction);

        return new TransactionOutput(transaction.id().value().toString(), transaction.description(),
                transaction.category(), transaction.amount(), transaction.occurredAt(), transaction.updatedAt());
    }
}
