package com.axdborges.voz.budgeting.infrastructure.http.request;

import com.axdborges.voz.budgeting.application.input.UpdateTransactionInput;
import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTransactionRequest(String description, Category category, BigDecimal amount, LocalDate date) {

    public UpdateTransactionInput toInput() {
        if (amount != null && amount.signum() <= 0) {
            throw new IllegalArgumentException("amount deve ser maior que zero");
        }
        return new UpdateTransactionInput(description, category, amount, date);
    }
}
