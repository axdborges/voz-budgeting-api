package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import com.axdborges.voz.budgeting.infrastructure.persistence.entity.TransactionEntity;
import com.axdborges.voz.budgeting.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return jpaRepository.findById(id.value().toString()).map(this::toDomain);
    }

    @Override
    public void deleteById(TransactionId id) {
        jpaRepository.deleteById(id.value().toString());
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(transaction.id().value().toString(), transaction.description(),
                transaction.category(), transaction.amount(), transaction.occurredAt(), transaction.updatedAt());
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(TransactionId.of(entity.getId()), entity.getDescription(), entity.getCategory(),
                entity.getAmount(), entity.getOccurredAt(), entity.getUpdatedAt());
    }
}
