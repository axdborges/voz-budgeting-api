package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.infrastructure.ai.AudioTranscriptionService;
import com.axdborges.voz.budgeting.infrastructure.ai.VoiceCommandInterpreter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoiceCommandController.class)
class VoiceCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AudioTranscriptionService audioTranscriptionService;

    @MockitoBean
    private VoiceCommandInterpreter voiceCommandInterpreter;

    @Test
    void shouldReturnTheTranscriptionAndTheInterpretedReply() throws Exception {
        MockMultipartFile audio = new MockMultipartFile("audio", "comando.mp3", "audio/mpeg", "conteudo".getBytes());
        when(audioTranscriptionService.transcribe(any())).thenReturn("Gastei 50 reais no mercado");
        when(voiceCommandInterpreter.interpret(eq("Gastei 50 reais no mercado")))
                .thenReturn("Entendi: registrar um gasto de R$ 50 no mercado.");

        mockMvc.perform(multipart("/voice-commands").file(audio))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "transcription": "Gastei 50 reais no mercado",
                          "reply": "Entendi: registrar um gasto de R$ 50 no mercado."
                        }
                        """));
    }

    @Test
    void shouldUseTheDefaultAudioFileWhenNoFileParamIsGiven() throws Exception {
        when(audioTranscriptionService.transcribe(eq(new FileSystemResource("audios-java/mercado.mp3"))))
                .thenReturn("O mercado da semana passada deu R$ 669,29.");
        when(voiceCommandInterpreter.interpret(eq("O mercado da semana passada deu R$ 669,29.")))
                .thenReturn("Transação registrada com sucesso: R$ 669,29 em MERCADO.");

        mockMvc.perform(get("/voice-commands/mock"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "transcription": "O mercado da semana passada deu R$ 669,29.",
                          "reply": "Transação registrada com sucesso: R$ 669,29 em MERCADO."
                        }
                        """));
    }

    @Test
    void shouldUseTheAudioFileGivenInTheFileParam() throws Exception {
        when(audioTranscriptionService.transcribe(eq(new FileSystemResource("audios-java/adicionar-saldo.mp3"))))
                .thenReturn("Quero colocar 50 reais na minha conta hoje.");
        when(voiceCommandInterpreter.interpret(eq("Quero colocar 50 reais na minha conta hoje.")))
                .thenReturn("Entendi: registrar um gasto de R$ 50.");

        mockMvc.perform(get("/voice-commands/mock").param("file", "adicionar-saldo.mp3"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "transcription": "Quero colocar 50 reais na minha conta hoje.",
                          "reply": "Entendi: registrar um gasto de R$ 50."
                        }
                        """));
    }
}
