package com.axdborges.voz.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.tts.TextToSpeechModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextToSpeechServiceTest {

    @Mock
    private TextToSpeechModel textToSpeechModel;

    @Test
    void shouldDelegateSynthesisToTheConfiguredModel() {
        var service = new TextToSpeechService(textToSpeechModel);
        byte[] audioBytes = {1, 2, 3};
        when(textToSpeechModel.call("Transação registrada com sucesso")).thenReturn(audioBytes);

        byte[] result = service.synthesize("Transação registrada com sucesso");

        assertThat(result).isEqualTo(audioBytes);
    }
}
