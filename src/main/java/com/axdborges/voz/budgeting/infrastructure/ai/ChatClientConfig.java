package com.axdborges.voz.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente financeiro que interpreta comandos de voz transcritos sobre
            transações financeiras pessoais.
            A pessoa pode querer registrar um gasto (valor, ação como "gastei"/"paguei"/"comprei",
            e um local ou categoria) ou consultar gastos já registrados (por categoria, por período, etc.).
            Identifique a intenção do comando e responda de forma breve e objetiva, indicando:
            - a ação identificada (registrar transação ou consultar transações);
            - os dados extraídos do comando (valor, categoria/estabelecimento, período, quando aplicável).
            Você ainda não executa nenhuma ação real, apenas descreve o que entendeu do comando.
            """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(SYSTEM_PROMPT).build();
    }
}
