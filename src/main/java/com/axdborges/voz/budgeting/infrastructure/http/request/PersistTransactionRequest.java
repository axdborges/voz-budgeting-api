package com.axdborges.voz.budgeting.infrastructure.http.request;

import com.axdborges.voz.budgeting.application.input.PersistTransactionInput;
import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PersistTransactionRequest(String description, Category category, BigDecimal amount, LocalDate date) {

    public PersistTransactionInput toInput() {
        if (category == null) {
            throw new IllegalArgumentException("category é obrigatório");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount é obrigatório e deve ser maior que zero");
        }
        return new PersistTransactionInput(description, category, amount, date);
    }
}
