package com.axdborges.voz.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

// TODO (Tarefa 5 - TODO.md): hoje só descreve a intenção em texto; quando o Tool Calling existir,
// o ChatClient deve registrar os use cases como tools e executar a ação de verdade.
@Service
public class VoiceCommandInterpreter {

    private final ChatClient chatClient;

    public VoiceCommandInterpreter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String interpret(String transcribedText) {
        return chatClient.prompt(transcribedText).call().content();
    }
}
