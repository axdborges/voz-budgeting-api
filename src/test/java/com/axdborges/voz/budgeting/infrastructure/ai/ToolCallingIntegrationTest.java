package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 5 - TODO.md): valida que um comando de voz claro realmente aciona a tool e persiste
// a transação (não só descreve a intenção em texto). Só roda quando OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class ToolCallingIntegrationTest {

    @Autowired
    private VoiceCommandInterpreter voiceCommandInterpreter;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldActuallyPersistTheTransactionWhenTheCommandIsClear() {
        String reply = voiceCommandInterpreter.interpret("Gastei 50 reais no mercado");

        assertThat(reply).isNotBlank();
        assertThat(transactionRepository.findByCategory(Category.MERCADO)).isNotEmpty();
        System.out.println("Resposta do modelo: " + reply);
    }
}
