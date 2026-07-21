package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import com.axdborges.voz.budgeting.infrastructure.persistence.entity.TransactionEntity;
import com.axdborges.voz.budgeting.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    public JpaTransactionRepository(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Transaction transaction) {
        jpaRepository.save(toEntity(transaction));
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        return jpaRepository.findByCategory(category).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(transaction.id().value(), transaction.description(), transaction.category(),
                transaction.amount(), transaction.occurredAt());
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(new TransactionId(entity.getId()), entity.getDescription(), entity.getCategory(),
                entity.getAmount(), entity.getOccurredAt());
    }
}
