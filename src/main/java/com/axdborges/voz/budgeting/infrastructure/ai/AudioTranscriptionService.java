package com.axdborges.voz.budgeting.infrastructure.ai;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class AudioTranscriptionService {

    private final TranscriptionModel transcriptionModel;

    public AudioTranscriptionService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcribe(Resource audio) {
        return transcriptionModel.transcribe(audio);
    }
}
