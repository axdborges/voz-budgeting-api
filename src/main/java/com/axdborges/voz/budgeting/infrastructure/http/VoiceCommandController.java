package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.infrastructure.ai.AudioTranscriptionService;
import com.axdborges.voz.budgeting.infrastructure.ai.TextToSpeechService;
import com.axdborges.voz.budgeting.infrastructure.ai.VoiceCommandInterpreter;
import com.axdborges.voz.budgeting.infrastructure.http.response.VoiceCommandResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/voice-commands")
public class VoiceCommandController {

    private static final MediaType AUDIO_MPEG = MediaType.valueOf("audio/mpeg");

    private final AudioTranscriptionService audioTranscriptionService;
    private final VoiceCommandInterpreter voiceCommandInterpreter;
    private final TextToSpeechService textToSpeechService;

    public VoiceCommandController(AudioTranscriptionService audioTranscriptionService,
                                   VoiceCommandInterpreter voiceCommandInterpreter,
                                   TextToSpeechService textToSpeechService) {
        this.audioTranscriptionService = audioTranscriptionService;
        this.voiceCommandInterpreter = voiceCommandInterpreter;
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> receive(@RequestParam("audio") MultipartFile audio,
                                      @RequestHeader(value = HttpHeaders.ACCEPT, required = false,
                                              defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept) {
        return process(audio.getResource(), accept);
    }

    // Rota de conveniência só para testes manuais em dev: processa um áudio de audios-java/
    // (pasta local, fora do controle de versão) sem precisar montar um multipart.
    // Não faz parte do desafio — remover antes de qualquer publicação/deploy real.
    @GetMapping("/mock")
    public ResponseEntity<?> receiveMock(@RequestParam(defaultValue = "mercado.mp3") String file,
                                          @RequestHeader(value = HttpHeaders.ACCEPT, required = false,
                                                  defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept) {
        return process(new FileSystemResource("audios-java/" + file), accept);
    }

    // Content negotiation manual (em vez de duas @RequestMapping com "produces" diferentes no mesmo
    // path): evita a resolução ambígua do Spring quando o Accept é "*/*" (padrão do curl/testes), que
    // poderia acabar escolhendo o handler de áudio por engano. Aqui a regra é explícita e só devolve
    // os bytes puros quando o cliente pedir "audio/..." de propósito (ex.: Accept: audio/mpeg no
    // Insomnia) — qualquer outro Accept (ausente, */*, application/json) continua devolvendo o JSON.
    private ResponseEntity<?> process(Resource audio, String accept) {
        String transcription = audioTranscriptionService.transcribe(audio);
        String reply = voiceCommandInterpreter.interpret(transcription);
        byte[] audioReply = textToSpeechService.synthesize(reply);

        if (ContentNegotiation.wantsRawAudio(accept)) {
            return ResponseEntity.ok().contentType(AUDIO_MPEG).body(audioReply);
        }

        String audioBase64 = Base64.getEncoder().encodeToString(audioReply);
        return ResponseEntity.ok(new VoiceCommandResponse(transcription, reply, audioBase64));
    }
}
