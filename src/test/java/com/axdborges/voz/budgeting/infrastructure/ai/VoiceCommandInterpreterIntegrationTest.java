package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 4 - TODO.md): valida que o ChatClient interpreta a intenção de comandos de voz
// transcritos. Só roda quando OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class VoiceCommandInterpreterIntegrationTest {

    @Autowired
    private VoiceCommandInterpreter voiceCommandInterpreter;

    @ParameterizedTest(name = "deve interpretar: {0}")
    @ValueSource(strings = {
            "Gastei 50 reais no mercado",
            "Quanto eu gastei em mercado esse mês?"
    })
    void shouldInterpretTheTranscribedVoiceCommand(String transcribedText) {
        String reply = voiceCommandInterpreter.interpret(transcribedText);

        assertThat(reply).isNotBlank();
        System.out.println("Comando: " + transcribedText + " -> Interpretação: " + reply);
    }
}
