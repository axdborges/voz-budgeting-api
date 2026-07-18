package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioTranscriptionServiceTest {

    @Mock
    private TranscriptionModel transcriptionModel;

    @Test
    void shouldDelegateTranscriptionToTheConfiguredModel() {
        var service = new AudioTranscriptionService(transcriptionModel);
        Resource audio = new ByteArrayResource("conteudo".getBytes());
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("Gastei 50 reais no mercado");

        String result = service.transcribe(audio);

        assertThat(result).isEqualTo("Gastei 50 reais no mercado");
    }
}
