package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldDeleteTheTransactionWhenItExists() {
        TransactionId id = TransactionId.generate();
        Transaction transaction = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.valueOf(50),
                LocalDateTime.now());
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        var useCase = new DeleteTransactionUseCase(transactionRepository);

        useCase.execute(id);

        verify(transactionRepository).deleteById(id);
    }

    @Test
    void shouldThrowAndNotDeleteWhenTheTransactionDoesNotExist() {
        TransactionId id = TransactionId.generate();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        var useCase = new DeleteTransactionUseCase(transactionRepository);

        assertThatThrownBy(() -> useCase.execute(id)).isInstanceOf(TransactionNotFoundException.class);
        verify(transactionRepository, never()).deleteById(id);
    }
}
