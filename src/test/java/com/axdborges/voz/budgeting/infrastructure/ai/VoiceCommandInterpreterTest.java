package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoiceCommandInterpreterTest {

    @Test
    void shouldSendTheTranscribedTextToTheChatClientAndReturnItsReply() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt("Gastei 50 reais no mercado").call().content())
                .thenReturn("Entendi: registrar um gasto de R$ 50 no mercado.");
        var interpreter = new VoiceCommandInterpreter(chatClient);

        String reply = interpreter.interpret("Gastei 50 reais no mercado");

        assertThat(reply).isEqualTo("Entendi: registrar um gasto de R$ 50 no mercado.");
    }
}
