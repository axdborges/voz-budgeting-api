package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.UpdateTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldApplyOnlyTheGivenFieldsAndSetUpdatedAt() {
        TransactionId id = TransactionId.generate();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        Transaction existing = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.valueOf(50),
                occurredAt);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));
        var useCase = new UpdateTransactionUseCase(transactionRepository);

        TransactionOutput output = useCase.execute(id, new UpdateTransactionInput(null, null,
                BigDecimal.valueOf(75), null));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.description()).isEqualTo("supermercado");
        assertThat(saved.category()).isEqualTo(Category.MERCADO);
        assertThat(saved.amount()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(saved.occurredAt()).isEqualTo(occurredAt);
        assertThat(saved.updatedAt()).isNotNull();

        assertThat(output.amount()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(output.updatedAt()).isNotNull();
    }

    @Test
    void shouldReplaceTheDateKeepingTheOriginalTimeOfDay() {
        TransactionId id = TransactionId.generate();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 7, 19, 10, 30);
        Transaction existing = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.valueOf(50),
                occurredAt);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));
        var useCase = new UpdateTransactionUseCase(transactionRepository);

        LocalDate newDate = LocalDate.of(2026, 7, 20);
        useCase.execute(id, new UpdateTransactionInput(null, null, null, newDate));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        assertThat(captor.getValue().occurredAt()).isEqualTo(LocalDateTime.of(2026, 7, 20, 10, 30));
    }

    @Test
    void shouldThrowWhenTheTransactionDoesNotExist() {
        TransactionId id = TransactionId.generate();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        var useCase = new UpdateTransactionUseCase(transactionRepository);
        var input = new UpdateTransactionInput(null, null, null, null);

        assertThatThrownBy(() -> useCase.execute(id, input)).isInstanceOf(TransactionNotFoundException.class);
    }
}
