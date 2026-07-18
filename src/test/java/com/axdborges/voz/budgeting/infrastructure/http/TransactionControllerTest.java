package com.axdborges.voz.budgeting.infrastructure.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // TODO (Tarefa 8 - TODO.md): substituir por testes dos endpoints reais quando forem implementados
    @Test
    void shouldReturnNotFoundBecauseNoEndpointIsMappedYet() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isNotFound());
    }
}
