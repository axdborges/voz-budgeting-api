package com.axdborges.voz.budgeting.infrastructure.ai;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.domain.Category;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SpendingReportNarratorTest {

    @Test
    void shouldAskTheChatClientToNarrateTheReportWhenThereAreCategories() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().system(any(String.class)).user(any(String.class)).call().content())
                .thenReturn("50% dos seus gastos são com moradia, 30% com lazer e 20% com mercado.");
        var narrator = new SpendingReportNarrator(chatClient);
        var report = new SpendingReportOutput(BigDecimal.valueOf(1000), List.of(
                new CategoryPercentageOutput(Category.MORADIA, BigDecimal.valueOf(500), BigDecimal.valueOf(50.0))));

        String narrative = narrator.narrate(report);

        assertThat(narrative).isEqualTo("50% dos seus gastos são com moradia, 30% com lazer e 20% com mercado.");
    }

    @Test
    void shouldReturnACannedMessageWithoutCallingTheChatClientWhenThereAreNoCategories() {
        ChatClient chatClient = mock(ChatClient.class);
        var narrator = new SpendingReportNarrator(chatClient);
        var report = new SpendingReportOutput(BigDecimal.ZERO, List.of());

        String narrative = narrator.narrate(report);

        assertThat(narrative).contains("não tem nenhuma transação");
        verifyNoInteractions(chatClient);
    }
}
