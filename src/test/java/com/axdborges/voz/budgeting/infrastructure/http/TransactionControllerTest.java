package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.DeleteTransactionUseCase;
import com.axdborges.voz.budgeting.application.FindTransactionByIdUseCase;
import com.axdborges.voz.budgeting.application.ListAllTransactionsUseCase;
import com.axdborges.voz.budgeting.application.ListTransactionsByCategoryUseCase;
import com.axdborges.voz.budgeting.application.PersistTransactionUseCase;
import com.axdborges.voz.budgeting.application.UpdateTransactionUseCase;
import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.application.input.UpdateTransactionInput;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAllTransactionsUseCase listAllTransactionsUseCase;

    @MockitoBean
    private ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    @MockitoBean
    private PersistTransactionUseCase persistTransactionUseCase;

    @MockitoBean
    private FindTransactionByIdUseCase findTransactionByIdUseCase;

    @MockitoBean
    private UpdateTransactionUseCase updateTransactionUseCase;

    @MockitoBean
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @Test
    void shouldListAllTransactionsWhenNoCategoryIsGiven() throws Exception {
        TransactionOutput output = new TransactionOutput("id-1", "Mercado da semana", Category.MERCADO,
                new BigDecimal("150.00"), LocalDateTime.of(2026, 7, 20, 10, 0));
        when(listAllTransactionsUseCase.execute()).thenReturn(List.of(output));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": "id-1",
                            "description": "Mercado da semana",
                            "category": "MERCADO",
                            "amount": 150.00,
                            "occurredAt": "2026-07-20T10:00:00"
                          }
                        ]
                        """));
    }

    @Test
    void shouldListTransactionsByCategoryWhenCategoryIsGiven() throws Exception {
        TransactionOutput output = new TransactionOutput("id-2", "Uber pro trabalho", Category.TRANSPORTE,
                new BigDecimal("23.50"), LocalDateTime.of(2026, 7, 19, 8, 30));
        when(listTransactionsByCategoryUseCase.execute(Category.TRANSPORTE)).thenReturn(List.of(output));

        mockMvc.perform(get("/transactions").param("category", "TRANSPORTE"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": "id-2",
                            "description": "Uber pro trabalho",
                            "category": "TRANSPORTE",
                            "amount": 23.50,
                            "occurredAt": "2026-07-19T08:30:00"
                          }
                        ]
                        """));
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIsInvalid() throws Exception {
        mockMvc.perform(get("/transactions").param("category", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateATransactionAndReturnCreated() throws Exception {
        TransactionOutput output = new TransactionOutput("id-3", "Livro de história", Category.EDUCACAO,
                new BigDecimal("89.90"), LocalDateTime.of(2026, 7, 21, 12, 0));
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class))).thenReturn(output);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "description": "Livro de história",
                                  "category": "EDUCACAO",
                                  "amount": 89.90,
                                  "date": "2026-07-21"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "id": "id-3",
                          "description": "Livro de história",
                          "category": "EDUCACAO",
                          "amount": 89.90,
                          "occurredAt": "2026-07-21T12:00:00"
                        }
                        """));

        verify(persistTransactionUseCase).execute(new PersistTransactionInput("Livro de história", Category.EDUCACAO,
                new BigDecimal("89.90"), LocalDate.of(2026, 7, 21)));
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIsMissingOnCreate() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 50.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("category é obrigatório"))
                .andExpect(jsonPath("$.path").value("/transactions"));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsMissingOnCreate() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "category": "MERCADO"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsZeroOrNegativeOnCreate() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "category": "MERCADO",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTheTransactionWhenFoundById() throws Exception {
        TransactionId id = TransactionId.generate();
        TransactionOutput output = new TransactionOutput(id.value().toString(), "supermercado", Category.MERCADO,
                new BigDecimal("50.00"), LocalDateTime.of(2026, 7, 20, 10, 0));
        when(findTransactionByIdUseCase.execute(id)).thenReturn(output);

        mockMvc.perform(get("/transactions/{id}", id.value()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": "%s",
                          "description": "supermercado",
                          "category": "MERCADO",
                          "amount": 50.00,
                          "occurredAt": "2026-07-20T10:00:00"
                        }
                        """.formatted(id.value())));
    }

    @Test
    void shouldReturnNotFoundWhenFindingAMissingId() throws Exception {
        TransactionId id = TransactionId.generate();
        when(findTransactionByIdUseCase.execute(id)).thenThrow(new TransactionNotFoundException(id));

        mockMvc.perform(get("/transactions/{id}", id.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/transactions/" + id.value()));
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/transactions/{id}", "nao-e-um-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateTheTransactionAndReturnOk() throws Exception {
        TransactionId id = TransactionId.generate();
        TransactionOutput output = new TransactionOutput(id.value().toString(), "supermercado", Category.MERCADO,
                new BigDecimal("75.00"), LocalDateTime.of(2026, 7, 19, 10, 0), LocalDateTime.of(2026, 7, 21, 9, 0));
        when(updateTransactionUseCase.execute(any(TransactionId.class), any(UpdateTransactionInput.class)))
                .thenReturn(output);

        mockMvc.perform(patch("/transactions/{id}", id.value())
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 75.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "amount": 75.00,
                          "updatedAt": "2026-07-21T09:00:00"
                        }
                        """));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingAMissingId() throws Exception {
        TransactionId id = TransactionId.generate();
        when(updateTransactionUseCase.execute(any(TransactionId.class), any(UpdateTransactionInput.class)))
                .thenThrow(new TransactionNotFoundException(id));

        mockMvc.perform(patch("/transactions/{id}", id.value())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithZeroOrNegativeAmount() throws Exception {
        mockMvc.perform(patch("/transactions/{id}", TransactionId.generate().value())
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": -10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteTheTransactionAndReturnNoContent() throws Exception {
        TransactionId id = TransactionId.generate();

        mockMvc.perform(delete("/transactions/{id}", id.value()))
                .andExpect(status().isNoContent());

        verify(deleteTransactionUseCase).execute(id);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingAMissingId() throws Exception {
        TransactionId id = TransactionId.generate();
        doThrow(new TransactionNotFoundException(id)).when(deleteTransactionUseCase).execute(id);

        mockMvc.perform(delete("/transactions/{id}", id.value()))
                .andExpect(status().isNotFound());
    }
}
