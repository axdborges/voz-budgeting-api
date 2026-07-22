package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.ListAllAuditLogsUseCase;
import com.axdborges.voz.budgeting.application.ListAuditLogsByTransactionUseCase;
import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAllAuditLogsUseCase listAllAuditLogsUseCase;

    @MockitoBean
    private ListAuditLogsByTransactionUseCase listAuditLogsByTransactionUseCase;

    @Test
    void shouldListAllAuditLogsWhenNoTransactionIdIsGiven() throws Exception {
        AuditLogOutput output = new AuditLogOutput(1L, "id-1", AuditAction.CREATED, "Categoria: MERCADO, valor: R$ 50",
                LocalDateTime.of(2026, 7, 22, 10, 0));
        when(listAllAuditLogsUseCase.execute()).thenReturn(List.of(output));

        mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].transactionId").value("id-1"))
                .andExpect(jsonPath("$[0].action").value("CREATED"));
    }

    @Test
    void shouldListAuditLogsFromTheGivenTransactionWhenTransactionIdIsGiven() throws Exception {
        TransactionId id = TransactionId.generate();
        AuditLogOutput output = new AuditLogOutput(2L, id.value().toString(), AuditAction.UPDATED,
                "valor: R$ 50 -> R$ 75", LocalDateTime.of(2026, 7, 22, 11, 0));
        when(listAuditLogsByTransactionUseCase.execute(id)).thenReturn(List.of(output));

        mockMvc.perform(get("/audit-logs").param("transactionId", id.value().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(id.value().toString()))
                .andExpect(jsonPath("$[0].action").value("UPDATED"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/audit-logs").param("transactionId", "nao-e-um-uuid"))
                .andExpect(status().isBadRequest());
    }
}
