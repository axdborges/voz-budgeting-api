package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.GenerateSpendingReportUseCase;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.infrastructure.ai.SpendingReportNarrator;
import com.axdborges.voz.budgeting.infrastructure.ai.TextToSpeechService;
import com.axdborges.voz.budgeting.infrastructure.http.response.ReportResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private static final MediaType AUDIO_MPEG = MediaType.valueOf("audio/mpeg");

    private final GenerateSpendingReportUseCase generateSpendingReportUseCase;
    private final SpendingReportNarrator spendingReportNarrator;
    private final TextToSpeechService textToSpeechService;

    public ReportController(GenerateSpendingReportUseCase generateSpendingReportUseCase,
                             SpendingReportNarrator spendingReportNarrator,
                             TextToSpeechService textToSpeechService) {
        this.generateSpendingReportUseCase = generateSpendingReportUseCase;
        this.spendingReportNarrator = spendingReportNarrator;
        this.textToSpeechService = textToSpeechService;
    }

    // Diferente de /voice-commands (que sempre sintetiza áudio, decisão da Tarefa 7): aqui o áudio só
    // é gerado quando o Accept realmente pede audio/*, pra não pagar uma chamada de TTS à toa em toda
    // consulta ao relatório (ex.: um dashboard que só quer o JSON).
    @GetMapping
    public ResponseEntity<?> report(@RequestHeader(value = HttpHeaders.ACCEPT, required = false,
            defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept) {
        SpendingReportOutput output = generateSpendingReportUseCase.execute();
        String narrative = spendingReportNarrator.narrate(output);

        if (ContentNegotiation.wantsRawAudio(accept)) {
            byte[] audio = textToSpeechService.synthesize(narrative);
            return ResponseEntity.ok().contentType(AUDIO_MPEG).body(audio);
        }

        return ResponseEntity.ok(ReportResponse.from(narrative, output));
    }
}
