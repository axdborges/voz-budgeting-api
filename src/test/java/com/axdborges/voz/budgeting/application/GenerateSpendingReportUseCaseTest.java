package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
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
class GenerateSpendingReportUseCaseTest {

    @Mock
    private ListAllTransactionsUseCase listAllTransactionsUseCase;

    @Test
    void shouldReturnZeroTotalAndNoCategoriesWhenThereAreNoTransactions() {
        when(listAllTransactionsUseCase.execute()).thenReturn(List.of());
        var useCase = new GenerateSpendingReportUseCase(listAllTransactionsUseCase);

        SpendingReportOutput report = useCase.execute();

        assertThat(report.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(report.categories()).isEmpty();
    }

    @Test
    void shouldComputeThePercentageOfEachCategoryOverTheTotal() {
        List<TransactionOutput> transactions = List.of(
                new TransactionOutput("1", "aluguel", Category.MORADIA, BigDecimal.valueOf(500), LocalDateTime.now()),
                new TransactionOutput("2", "cinema", Category.LAZER, BigDecimal.valueOf(300), LocalDateTime.now()),
                new TransactionOutput("3", "mercado", Category.MERCADO, BigDecimal.valueOf(200), LocalDateTime.now()));
        when(listAllTransactionsUseCase.execute()).thenReturn(transactions);
        var useCase = new GenerateSpendingReportUseCase(listAllTransactionsUseCase);

        SpendingReportOutput report = useCase.execute();

        assertThat(report.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(report.categories()).hasSize(3);
        assertThat(report.categories()).extracting(CategoryPercentageOutput::category)
                .containsExactly(Category.MORADIA, Category.LAZER, Category.MERCADO);
        assertThat(report.categories().get(0).percentage()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(report.categories().get(1).percentage()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(report.categories().get(2).percentage()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
    }

    @Test
    void shouldSumMultipleTransactionsFromTheSameCategoryBeforeComputingThePercentage() {
        List<TransactionOutput> transactions = List.of(
                new TransactionOutput("1", "mercado 1", Category.MERCADO, BigDecimal.valueOf(30), LocalDateTime.now()),
                new TransactionOutput("2", "mercado 2", Category.MERCADO, BigDecimal.valueOf(70), LocalDateTime.now()));
        when(listAllTransactionsUseCase.execute()).thenReturn(transactions);
        var useCase = new GenerateSpendingReportUseCase(listAllTransactionsUseCase);

        SpendingReportOutput report = useCase.execute();

        assertThat(report.categories()).hasSize(1);
        assertThat(report.categories().get(0).totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(report.categories().get(0).percentage()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
    }
}
