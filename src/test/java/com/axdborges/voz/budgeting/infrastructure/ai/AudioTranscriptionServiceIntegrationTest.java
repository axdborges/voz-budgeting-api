package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (Tarefa 3 - TODO.md): valida a transcrição com um áudio real de audios-java/ (pasta local,
// fora do controle de versão). Só roda quando OPENAI_API_KEY está definida.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class AudioTranscriptionServiceIntegrationTest {

    @Autowired
    private AudioTranscriptionService audioTranscriptionService;

    @Test
    void shouldTranscribeTheSampleAudioFile() {
        Resource audio = new FileSystemResource("audios-java/meu-nome.mp3");

        String transcription = audioTranscriptionService.transcribe(audio);

        assertThat(transcription).isNotBlank();
        System.out.println("Transcrição de meu-nome.mp3: " + transcription);
    }
}
