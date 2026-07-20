package com.axdborges.voz.budgeting.application.input;

import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PersistTransactionInput(String description, Category category, BigDecimal amount, LocalDate date) {
}
