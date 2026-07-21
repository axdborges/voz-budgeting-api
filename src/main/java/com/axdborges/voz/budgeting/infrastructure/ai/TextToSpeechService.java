package com.axdborges.voz.budgeting.infrastructure.ai;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;

@Service
public class TextToSpeechService {

    private final TextToSpeechModel textToSpeechModel;

    public TextToSpeechService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] synthesize(String text) {
        return textToSpeechModel.call(text);
    }
}
