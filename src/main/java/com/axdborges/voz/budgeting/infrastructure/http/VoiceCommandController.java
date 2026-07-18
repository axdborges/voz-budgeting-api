package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.infrastructure.ai.AudioTranscriptionService;
import com.axdborges.voz.budgeting.infrastructure.ai.VoiceCommandInterpreter;
import com.axdborges.voz.budgeting.infrastructure.http.response.VoiceCommandResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// TODO (Tarefa 5 - TODO.md): a interpretação ainda só descreve a intenção em texto; falta o
// Tool Calling registrar os use cases reais (persistir/consultar transação) no ChatClient.
@RestController
@RequestMapping("/voice-commands")
public class VoiceCommandController {

    private final AudioTranscriptionService audioTranscriptionService;
    private final VoiceCommandInterpreter voiceCommandInterpreter;

    public VoiceCommandController(AudioTranscriptionService audioTranscriptionService,
                                   VoiceCommandInterpreter voiceCommandInterpreter) {
        this.audioTranscriptionService = audioTranscriptionService;
        this.voiceCommandInterpreter = voiceCommandInterpreter;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VoiceCommandResponse> receive(@RequestParam("audio") MultipartFile audio) {
        return ResponseEntity.ok(process(audio.getResource()));
    }

    // Rota de conveniência só para testes manuais em dev: processa um áudio de audios-java/
    // (pasta local, fora do controle de versão) sem precisar montar um multipart.
    // Não faz parte do desafio — remover antes de qualquer publicação/deploy real.
    @GetMapping("/mock")
    public ResponseEntity<VoiceCommandResponse> receiveMock(
            @RequestParam(defaultValue = "meu-nome.mp3") String file) {
        return ResponseEntity.ok(process(new FileSystemResource("audios-java/" + file)));
    }

    private VoiceCommandResponse process(Resource audio) {
        String transcription = audioTranscriptionService.transcribe(audio);
        String reply = voiceCommandInterpreter.interpret(transcription);
        return new VoiceCommandResponse(transcription, reply);
    }
}
