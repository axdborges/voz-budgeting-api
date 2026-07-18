package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.infrastructure.ai.AudioTranscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoiceCommandController.class)
class VoiceCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AudioTranscriptionService audioTranscriptionService;

    @Test
    void shouldReturnTheTranscribedText() throws Exception {
        MockMultipartFile audio = new MockMultipartFile("audio", "comando.mp3", "audio/mpeg", "conteudo".getBytes());
        when(audioTranscriptionService.transcribe(any())).thenReturn("Gastei 50 reais no mercado");

        mockMvc.perform(multipart("/voice-commands").file(audio))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"text\":\"Gastei 50 reais no mercado\"}"));
    }
}
