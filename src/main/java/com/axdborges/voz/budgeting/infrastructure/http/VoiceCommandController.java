package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.infrastructure.ai.AudioTranscriptionService;
import com.axdborges.voz.budgeting.infrastructure.http.response.TranscriptionResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// TODO (Tarefa 4/5 - TODO.md): hoje só devolve o texto transcrito; quando o ChatClient/Tool Calling
// existir, este endpoint deve orquestrar a interpretação de intenção e devolver a resposta final.
@RestController
@RequestMapping("/voice-commands")
public class VoiceCommandController {

    private final AudioTranscriptionService audioTranscriptionService;

    public VoiceCommandController(AudioTranscriptionService audioTranscriptionService) {
        this.audioTranscriptionService = audioTranscriptionService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<TranscriptionResponse> receive(@RequestParam("audio") MultipartFile audio) {
        String text = audioTranscriptionService.transcribe(audio.getResource());
        return ResponseEntity.ok(new TranscriptionResponse(text));
    }

    // Rota de conveniência só para testes manuais em dev: transcreve um áudio fixo de
    // audios-java/ (pasta local, fora do controle de versão) sem precisar montar um multipart.
    // Não faz parte do desafio — remover antes de qualquer publicação/deploy real.
    @GetMapping("/mock")
    public ResponseEntity<TranscriptionResponse> receiveMock() {
        var audio = new FileSystemResource("audios-java/meu-nome.mp3");
        String text = audioTranscriptionService.transcribe(audio);
        return ResponseEntity.ok(new TranscriptionResponse(text));
    }
}
