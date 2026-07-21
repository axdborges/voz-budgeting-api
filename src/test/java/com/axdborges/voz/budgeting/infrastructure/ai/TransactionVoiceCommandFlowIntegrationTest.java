package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// TODO (evolução: consulta geral por categoria): valida o fluxo real de ponta a ponta — áudio real
// (mercado.mp3, uber.mp3, livros.mp3) transcrito e interpretado registra transações de verdade em
// categorias diferentes, e o áudio consulta-todos.mp3 devolve o resumo agrupado por categoria com os
// valores corretos. Só roda quando OPENAI_API_KEY está definida.
// @DirtiesContext garante um TransactionRepository (JPA/H2 em memória) limpo pra esta classe, sem
// contaminação de transações persistidas por outros testes de integração que rodam no mesmo processo.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TransactionVoiceCommandFlowIntegrationTest {

    @Autowired
    private AudioTranscriptionService audioTranscriptionService;

    @Autowired
    private VoiceCommandInterpreter voiceCommandInterpreter;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldRegisterTransactionsFromRealAudioAndSummarizeThemAllByCategory() {
        String mercadoReply = processAudio("mercado.mp3");
        String uberReply = processAudio("uber.mp3");
        String livrosReply = processAudio("livros.mp3");

        assertThat(mercadoReply).containsIgnoringCase("MERCADO");
        assertThat(uberReply).containsIgnoringCase("TRANSPORTE");
        assertThat(livrosReply).containsIgnoringCase("EDUCA");

        assertThat(transactionRepository.findByCategory(Category.MERCADO)).hasSize(1);
        assertThat(transactionRepository.findByCategory(Category.TRANSPORTE)).hasSize(1);
        assertThat(transactionRepository.findByCategory(Category.EDUCACAO)).hasSize(1);

        String resumoReply = processAudio("consulta-todos.mp3");

        assertThat(resumoReply)
                .containsIgnoringCase("MERCADO")
                .containsIgnoringCase("TRANSPORTE")
                .containsIgnoringCase("EDUCA");
        System.out.println("Resumo de todas as transações: " + resumoReply);
    }

    private String processAudio(String fileName) {
        String transcription = audioTranscriptionService.transcribe(new FileSystemResource("audios-java/" + fileName));
        String reply = voiceCommandInterpreter.interpret(transcription);
        System.out.println(fileName + " -> transcrição: " + transcription + " | resposta: " + reply);
        return reply;
    }
}
