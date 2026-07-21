package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 7 - TODO.md): valida a geração de voz real com o TTS da OpenAI. Só roda quando
// OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@ActiveProfiles("test")
class TextToSpeechServiceIntegrationTest {

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Test
    void shouldSynthesizeSpeechFromText() {
        byte[] audio = textToSpeechService.synthesize("Transação registrada com sucesso.");

        assertThat(audio).isNotEmpty();
        System.out.println("Áudio gerado: " + audio.length + " bytes");
    }
}
