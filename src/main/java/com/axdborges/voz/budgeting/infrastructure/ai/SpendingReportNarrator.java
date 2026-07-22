package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SpendingReportNarrator {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final String SYSTEM_PROMPT = """
            Você é um assistente financeiro. Você vai receber um resumo numérico de gastos, já
            calculado, com o valor total e a porcentagem de cada categoria em relação ao total.

            Escreva um relatório curto e natural em português (pt-BR), citando cada categoria e sua
            porcentagem, no estilo "X% dos seus gastos são com Y". Use exatamente os números
            fornecidos — não recalcule, não arredonde diferente do que foi passado, não invente
            nenhuma categoria ou valor que não esteja nos dados. Não chame nenhuma ferramenta: sua
            única tarefa aqui é escrever o texto do relatório.
            """;

    private final ChatClient chatClient;

    public SpendingReportNarrator(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String narrate(SpendingReportOutput report) {
        if (report.categories().isEmpty()) {
            return "Você ainda não tem nenhuma transação registrada, então não há relatório de gastos para gerar.";
        }

        String dados = report.categories().stream()
                .map(this::formatCategoryLine)
                .collect(Collectors.joining("\n"));

        String userMessage = String.format(PT_BR, "Total gasto: R$ %.2f%n%s", report.totalAmount(), dados);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
    }

    private String formatCategoryLine(CategoryPercentageOutput categoryPercentage) {
        return String.format(PT_BR, "- %s: R$ %.2f (%.1f%%)", categoryPercentage.category(),
                categoryPercentage.totalAmount(), categoryPercentage.percentage());
    }
}
