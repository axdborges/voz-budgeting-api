package com.axdborges.voz.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 2 - TODO.md): valida que a aplicação consegue se comunicar com o provedor de IA.
// Só roda quando OPENAI_API_KEY está definida (evita falhar/gastar crédito em ambientes sem a chave).
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiChatModelIntegrationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    void shouldReceiveAResponseFromTheConfiguredOpenAiModel() {
        String response = chatModel.call("Responda apenas com a palavra: ok");

        assertThat(response).isNotBlank();
    }
}
