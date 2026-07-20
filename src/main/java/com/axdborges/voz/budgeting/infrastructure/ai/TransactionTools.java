package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.application.ListTransactionsByCategoryUseCase;
import com.axdborges.voz.budgeting.application.PersistTransactionUseCase;
import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Component
public class TransactionTools {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    public TransactionTools(PersistTransactionUseCase persistTransactionUseCase,
                             ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
    }

    @Tool(description = "Registra uma nova transação financeira (gasto) do usuário. "
            + "Só chame esta ferramenta quando o comando tiver uma ação clara de registrar um gasto e um valor em reais.")
    public String registrarTransacao(
            @ToolParam(description = "Categoria da transação") Category categoria,
            @ToolParam(description = "Valor da transação em reais") BigDecimal valor,
            @ToolParam(description = "Descrição curta da transação, ex.: nome do estabelecimento ou do gasto. "
                    + "Deixe nulo se o comando não trouxer essa informação.", required = false) String descricao,
            @ToolParam(description = "Data da transação no formato ISO yyyy-MM-dd, já resolvida a partir da data de "
                    + "hoje informada no prompt (ex.: 'ontem' vira a data de ontem). Deixe nulo se o comando não "
                    + "mencionar uma data — nesse caso será usada a data de hoje.", required = false) String data) {
        LocalDate dataTransacao = parseData(data);

        TransactionOutput output = persistTransactionUseCase.execute(
                new PersistTransactionInput(descricao, categoria, valor, dataTransacao));

        String descricaoTexto = output.description() != null && !output.description().isBlank()
                ? " (" + output.description() + ")"
                : "";

        return String.format(PT_BR, "Transação registrada com sucesso: R$ %.2f em %s%s no dia %s, id %s.",
                output.amount(), output.category(), descricaoTexto, output.occurredAt().toLocalDate(),
                output.id());
    }

    private LocalDate parseData(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Tool(description = "Consulta as transações já registradas em uma categoria específica, com o total gasto. "
            + "Só chame esta ferramenta quando o comando for claramente uma pergunta sobre gastos/saldo já existentes.")
    public String consultarTransacoesPorCategoria(
            @ToolParam(description = "Categoria a consultar") Category categoria) {
        List<TransactionOutput> transacoes = listTransactionsByCategoryUseCase.execute(categoria);

        if (transacoes.isEmpty()) {
            return "Nenhuma transação registrada na categoria " + categoria + " até agora.";
        }

        BigDecimal total = transacoes.stream()
                .map(TransactionOutput::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return String.format(PT_BR, "Você tem %d transação(ões) na categoria %s, totalizando R$ %.2f.",
                transacoes.size(), categoria, total);
    }
}
