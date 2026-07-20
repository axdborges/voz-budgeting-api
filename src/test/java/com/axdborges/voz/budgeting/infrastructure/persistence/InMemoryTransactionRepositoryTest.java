package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTransactionRepositoryTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

    @Test
    void shouldFindOnlyTransactionsFromTheGivenCategory() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        Transaction transporte = new Transaction(TransactionId.generate(), "uber", Category.TRANSPORTE,
                BigDecimal.ONE, LocalDateTime.now());
        repository.save(mercado);
        repository.save(transporte);

        assertThat(repository.findByCategory(Category.MERCADO)).containsExactly(mercado);
    }

    @Test
    void shouldReturnAllSavedTransactions() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        Transaction transporte = new Transaction(TransactionId.generate(), "uber", Category.TRANSPORTE,
                BigDecimal.ONE, LocalDateTime.now());
        repository.save(mercado);
        repository.save(transporte);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(mercado, transporte);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionMatchesTheCategory() {
        assertThat(repository.findByCategory(Category.LAZER)).isEmpty();
    }
}
