package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.application.ListTransactionsByCategoryUseCase;
import com.axdborges.voz.budgeting.application.PersistTransactionUseCase;
import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionToolsTest {

    @Mock
    private PersistTransactionUseCase persistTransactionUseCase;

    @Mock
    private ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    @Test
    void shouldPersistTheTransactionAndSummarizeTheResult() {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenReturn(new TransactionOutput("abc-123", "mercado", Category.MERCADO,
                        BigDecimal.valueOf(50), LocalDateTime.now()));
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        String result = tools.registrarTransacao(Category.MERCADO, BigDecimal.valueOf(50), "mercado", null);

        assertThat(result).contains("50,00").contains("MERCADO").contains("mercado").contains("abc-123");
    }

    @Test
    void shouldPersistTheTransactionWithoutDescriptionWhenNoneIsGiven() {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenReturn(new TransactionOutput("abc-123", null, Category.MERCADO,
                        BigDecimal.valueOf(50), LocalDateTime.now()));
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        String result = tools.registrarTransacao(Category.MERCADO, BigDecimal.valueOf(50), null, null);

        assertThat(result).contains("50,00").contains("MERCADO").contains("abc-123").doesNotContain("null");
    }

    @Test
    void shouldForwardTheResolvedDateToTheUseCase() {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenReturn(new TransactionOutput("abc-123", "mercado", Category.MERCADO,
                        BigDecimal.valueOf(50), LocalDateTime.of(2026, 7, 19, 10, 0)));
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        tools.registrarTransacao(Category.MERCADO, BigDecimal.valueOf(50), "mercado", "2026-07-19");

        ArgumentCaptor<PersistTransactionInput> captor = ArgumentCaptor.forClass(PersistTransactionInput.class);
        verify(persistTransactionUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isEqualTo(LocalDate.of(2026, 7, 19));
    }

    @Test
    void shouldIgnoreAnUnparsableDateAndUseNullInstead() {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenReturn(new TransactionOutput("abc-123", "mercado", Category.MERCADO,
                        BigDecimal.valueOf(50), LocalDateTime.now()));
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        tools.registrarTransacao(Category.MERCADO, BigDecimal.valueOf(50), "mercado", "não é uma data");

        ArgumentCaptor<PersistTransactionInput> captor = ArgumentCaptor.forClass(PersistTransactionInput.class);
        verify(persistTransactionUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isNull();
    }

    @Test
    void shouldSummarizeTransactionsWhenThereAreMatches() {
        when(listTransactionsByCategoryUseCase.execute(Category.MERCADO)).thenReturn(List.of(
                new TransactionOutput("1", "mercado 1", Category.MERCADO, BigDecimal.valueOf(30), LocalDateTime.now()),
                new TransactionOutput("2", "mercado 2", Category.MERCADO, BigDecimal.valueOf(20), LocalDateTime.now())
        ));
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        String result = tools.consultarTransacoesPorCategoria(Category.MERCADO);

        assertThat(result).contains("2").contains("MERCADO").contains("50,00");
    }

    @Test
    void shouldInformThereAreNoTransactionsWhenListIsEmpty() {
        when(listTransactionsByCategoryUseCase.execute(Category.LAZER)).thenReturn(List.of());
        var tools = new TransactionTools(persistTransactionUseCase, listTransactionsByCategoryUseCase);

        String result = tools.consultarTransacoesPorCategoria(Category.LAZER);

        assertThat(result).contains("Nenhuma transação").contains("LAZER");
    }
}
