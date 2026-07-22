package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.GenerateSpendingReportUseCase;
import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.infrastructure.ai.SpendingReportNarrator;
import com.axdborges.voz.budgeting.infrastructure.ai.TextToSpeechService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateSpendingReportUseCase generateSpendingReportUseCase;

    @MockitoBean
    private SpendingReportNarrator spendingReportNarrator;

    @MockitoBean
    private TextToSpeechService textToSpeechService;

    @Test
    void shouldReturnTheReportAsJsonByDefault() throws Exception {
        var output = new SpendingReportOutput(BigDecimal.valueOf(1000), List.of(
                new CategoryPercentageOutput(Category.MORADIA, BigDecimal.valueOf(500), BigDecimal.valueOf(50.0))));
        when(generateSpendingReportUseCase.execute()).thenReturn(output);
        when(spendingReportNarrator.narrate(output)).thenReturn("50% dos seus gastos são com moradia.");

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("50% dos seus gastos são com moradia."))
                .andExpect(jsonPath("$.totalAmount").value(1000))
                .andExpect(jsonPath("$.categories[0].category").value("MORADIA"))
                .andExpect(jsonPath("$.categories[0].percentage").value(50.0));
    }

    @Test
    void shouldReturnRawAudioWhenAcceptIsAudioMpeg() throws Exception {
        var output = new SpendingReportOutput(BigDecimal.valueOf(1000), List.of(
                new CategoryPercentageOutput(Category.MORADIA, BigDecimal.valueOf(500), BigDecimal.valueOf(50.0))));
        byte[] audioBytes = {1, 2, 3};
        when(generateSpendingReportUseCase.execute()).thenReturn(output);
        when(spendingReportNarrator.narrate(output)).thenReturn("50% dos seus gastos são com moradia.");
        when(textToSpeechService.synthesize("50% dos seus gastos são com moradia.")).thenReturn(audioBytes);

        mockMvc.perform(get("/reports").header("Accept", "audio/mpeg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "audio/mpeg"))
                .andExpect(content().bytes(audioBytes));
    }

    @Test
    void shouldReturnJsonWhenAcceptIsWildcard() throws Exception {
        var output = new SpendingReportOutput(BigDecimal.ZERO, List.of());
        when(generateSpendingReportUseCase.execute()).thenReturn(output);
        when(spendingReportNarrator.narrate(output)).thenReturn("Você ainda não tem nenhuma transação registrada.");

        mockMvc.perform(get("/reports").header("Accept", "*/*"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isEmpty());
    }
}
