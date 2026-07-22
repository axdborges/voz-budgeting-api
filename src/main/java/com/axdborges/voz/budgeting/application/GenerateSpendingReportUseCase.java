package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GenerateSpendingReportUseCase {

    private final ListAllTransactionsUseCase listAllTransactionsUseCase;

    public GenerateSpendingReportUseCase(ListAllTransactionsUseCase listAllTransactionsUseCase) {
        this.listAllTransactionsUseCase = listAllTransactionsUseCase;
    }

    public SpendingReportOutput execute() {
        List<TransactionOutput> transactions = listAllTransactionsUseCase.execute();

        if (transactions.isEmpty()) {
            return new SpendingReportOutput(BigDecimal.ZERO, List.of());
        }

        Map<Category, BigDecimal> totalsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(TransactionOutput::category, () -> new EnumMap<>(Category.class),
                        Collectors.reducing(BigDecimal.ZERO, TransactionOutput::amount, BigDecimal::add)));

        BigDecimal total = totalsByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryPercentageOutput> categories = totalsByCategory.entrySet().stream()
                .map(entry -> new CategoryPercentageOutput(entry.getKey(), entry.getValue(), percentageOf(entry.getValue(), total)))
                .sorted(Comparator.comparing(CategoryPercentageOutput::percentage).reversed())
                .toList();

        return new SpendingReportOutput(total, categories);
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal total) {
        return amount.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }
}
