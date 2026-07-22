package com.axdborges.voz.budgeting.infrastructure.http.response;

import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(String id, String description, Category category, BigDecimal amount,
                                    LocalDateTime occurredAt, LocalDateTime updatedAt) {

    public static TransactionResponse from(TransactionOutput output) {
        return new TransactionResponse(output.id(), output.description(), output.category(), output.amount(),
                output.occurredAt(), output.updatedAt());
    }
}
