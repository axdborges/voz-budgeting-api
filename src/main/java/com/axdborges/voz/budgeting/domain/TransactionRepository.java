package com.axdborges.voz.budgeting.domain;

import java.util.List;

public interface TransactionRepository {

    void save(Transaction transaction);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findAll();
}
