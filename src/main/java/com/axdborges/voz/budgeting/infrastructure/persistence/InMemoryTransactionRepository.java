package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO (Tarefa 6 - TODO.md): substituir por um adapter JPA (TransactionJpaRepository) com banco real.
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    @Override
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        return transactions.stream()
                .filter(transaction -> transaction.category() == category)
                .toList();
    }

    @Override
    public List<Transaction> findAll() {
        return List.copyOf(transactions);
    }
}
