package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.domain.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Valida que o ChatClient real transforma o resumo numérico já calculado num texto de relatório —
// não recalcula nada, só narra. Só roda quando OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@ActiveProfiles("test")
class SpendingReportNarratorIntegrationTest {

    @Autowired
    private SpendingReportNarrator spendingReportNarrator;

    @Test
    void shouldNarrateTheSpendingReportInNaturalLanguage() {
        var report = new SpendingReportOutput(BigDecimal.valueOf(1000), List.of(
                new CategoryPercentageOutput(Category.MORADIA, BigDecimal.valueOf(500), BigDecimal.valueOf(50.0)),
                new CategoryPercentageOutput(Category.LAZER, BigDecimal.valueOf(300), BigDecimal.valueOf(30.0)),
                new CategoryPercentageOutput(Category.MERCADO, BigDecimal.valueOf(200), BigDecimal.valueOf(20.0))));

        String narrative = spendingReportNarrator.narrate(report);

        assertThat(narrative).isNotBlank();
        System.out.println("Relatório gerado: " + narrative);
    }
}
