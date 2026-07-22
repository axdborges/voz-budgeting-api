package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTransactionsByCategoryUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldReturnTransactionsFromTheRepositoryAsOutput() {
        Transaction transaction = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.valueOf(50), LocalDateTime.now());
        when(transactionRepository.findByCategory(Category.MERCADO)).thenReturn(List.of(transaction));
        var useCase = new ListTransactionsByCategoryUseCase(transactionRepository);

        List<TransactionOutput> outputs = useCase.execute(Category.MERCADO);

        assertThat(outputs).hasSize(1);
        TransactionOutput output = outputs.get(0);
        assertThat(output.id()).isEqualTo(transaction.id().value().toString());
        assertThat(output.description()).isEqualTo("supermercado");
        assertThat(output.category()).isEqualTo(Category.MERCADO);
        assertThat(output.amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void shouldReturnEmptyListWhenTheRepositoryHasNoMatch() {
        when(transactionRepository.findByCategory(Category.LAZER)).thenReturn(List.of());
        var useCase = new ListTransactionsByCategoryUseCase(transactionRepository);

        assertThat(useCase.execute(Category.LAZER)).isEmpty();
    }
}
