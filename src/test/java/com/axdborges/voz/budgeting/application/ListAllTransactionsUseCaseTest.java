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
class ListAllTransactionsUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldReturnAllTransactionsFromTheRepositoryAsOutput() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.valueOf(50), LocalDateTime.now());
        Transaction transporte = new Transaction(TransactionId.generate(), "uber", Category.TRANSPORTE,
                BigDecimal.valueOf(12.5), LocalDateTime.now());
        when(transactionRepository.findAll()).thenReturn(List.of(mercado, transporte));
        var useCase = new ListAllTransactionsUseCase(transactionRepository);

        List<TransactionOutput> outputs = useCase.execute();

        assertThat(outputs).hasSize(2);
        assertThat(outputs).extracting(TransactionOutput::category)
                .containsExactlyInAnyOrder(Category.MERCADO, Category.TRANSPORTE);
    }

    @Test
    void shouldReturnEmptyListWhenTheRepositoryHasNoTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of());
        var useCase = new ListAllTransactionsUseCase(transactionRepository);

        assertThat(useCase.execute()).isEmpty();
    }
}
