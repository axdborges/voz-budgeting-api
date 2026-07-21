package com.axdborges.voz.budgeting.infrastructure.http.response;

public record VoiceCommandResponse(String transcription, String reply, String audioBase64) {
}
