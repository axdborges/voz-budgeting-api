package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PersistTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveTheTransactionAndReturnItsData() {
        var useCase = new PersistTransactionUseCase(transactionRepository);
        var input = new PersistTransactionInput("supermercado", Category.MERCADO, BigDecimal.valueOf(50), null);

        TransactionOutput output = useCase.execute(input);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.id().value()).isNotNull();
        assertThat(saved.description()).isEqualTo("supermercado");
        assertThat(saved.category()).isEqualTo(Category.MERCADO);
        assertThat(saved.amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(saved.occurredAt().toLocalDate()).isEqualTo(LocalDate.now());

        assertThat(output.id()).isEqualTo(saved.id().value().toString());
        assertThat(output.description()).isEqualTo("supermercado");
        assertThat(output.category()).isEqualTo(Category.MERCADO);
        assertThat(output.amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void shouldSaveTheTransactionWithoutDescriptionWhenNoneIsGiven() {
        var useCase = new PersistTransactionUseCase(transactionRepository);
        var input = new PersistTransactionInput(null, Category.LAZER, BigDecimal.valueOf(20), null);

        TransactionOutput output = useCase.execute(input);

        assertThat(output.description()).isNull();
        assertThat(output.category()).isEqualTo(Category.LAZER);
    }

    @Test
    void shouldUseTheGivenDateWhenPresentInsteadOfToday() {
        var useCase = new PersistTransactionUseCase(transactionRepository);
        LocalDate givenDate = LocalDate.now().minusDays(3);
        var input = new PersistTransactionInput("padaria", Category.MERCADO, BigDecimal.valueOf(15), givenDate);

        TransactionOutput output = useCase.execute(input);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        assertThat(captor.getValue().occurredAt().toLocalDate()).isEqualTo(givenDate);
        assertThat(output.occurredAt().toLocalDate()).isEqualTo(givenDate);
    }
}
