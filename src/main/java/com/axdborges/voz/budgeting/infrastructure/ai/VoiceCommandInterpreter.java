package com.axdborges.voz.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VoiceCommandInterpreter {

    private final ChatClient chatClient;

    public VoiceCommandInterpreter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String interpret(String transcribedText) {
        String systemPrompt = ChatClientConfig.SYSTEM_PROMPT_TEMPLATE.formatted(LocalDate.now());

        return chatClient.prompt()
                .system(systemPrompt)
                .user(transcribedText)
                .call()
                .content();
    }
}
