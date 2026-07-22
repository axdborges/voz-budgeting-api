package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class FindTransactionByIdUseCase {

    private final TransactionRepository transactionRepository;

    public FindTransactionByIdUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionOutput execute(TransactionId id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return new TransactionOutput(transaction.id().value().toString(), transaction.description(),
                transaction.category(), transaction.amount(), transaction.occurredAt(), transaction.updatedAt());
    }
}
