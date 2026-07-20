package com.axdborges.voz.budgeting.application.output;

import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionOutput(String id, String description, Category category, BigDecimal amount,
                                 LocalDateTime occurredAt) {
}
