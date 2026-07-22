package com.axdborges.voz.budgeting.domain;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findAll();

    Optional<Transaction> findById(TransactionId id);

    void deleteById(TransactionId id);
}
