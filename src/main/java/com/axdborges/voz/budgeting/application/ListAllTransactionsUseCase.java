package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAllTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public ListAllTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionOutput> execute() {
        return transactionRepository.findAll().stream()
                .map(this::toOutput)
                .toList();
    }

    private TransactionOutput toOutput(Transaction transaction) {
        return new TransactionOutput(transaction.id().value(), transaction.description(), transaction.category(),
                transaction.amount(), transaction.occurredAt());
    }
}
