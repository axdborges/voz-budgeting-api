package com.axdborges.voz.budgeting.application.output;

import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;

public record CategoryPercentageOutput(Category category, BigDecimal totalAmount, BigDecimal percentage) {
}
