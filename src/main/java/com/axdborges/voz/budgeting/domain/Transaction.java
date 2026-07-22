package com.axdborges.voz.budgeting.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(TransactionId id, String description, Category category, BigDecimal amount,
                           LocalDateTime occurredAt, LocalDateTime updatedAt) {

    public Transaction(TransactionId id, String description, Category category, BigDecimal amount,
                        LocalDateTime occurredAt) {
        this(id, description, category, amount, occurredAt, null);
    }
}
