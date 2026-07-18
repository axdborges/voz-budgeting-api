package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 3 - TODO.md): valida a transcrição com áudios reais de audios-java/ (pasta local,
// fora do controle de versão). Só roda quando OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class AudioTranscriptionServiceIntegrationTest {

    @Autowired
    private AudioTranscriptionService audioTranscriptionService;

    @ParameterizedTest(name = "deve transcrever {0}")
    @ValueSource(strings = {"meu-nome.mp3", "nome-idade.mp3"})
    void shouldTranscribeTheSampleAudioFile(String fileName) {
        Resource audio = new FileSystemResource("audios-java/" + fileName);

        String transcription = audioTranscriptionService.transcribe(audio);

        assertThat(transcription).isNotBlank();
        System.out.println("Transcrição de " + fileName + ": " + transcription);
    }
}
