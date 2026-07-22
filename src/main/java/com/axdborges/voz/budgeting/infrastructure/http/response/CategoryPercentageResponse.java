package com.axdborges.voz.budgeting.infrastructure.http.response;

import com.axdborges.voz.budgeting.application.output.CategoryPercentageOutput;
import com.axdborges.voz.budgeting.domain.Category;

import java.math.BigDecimal;

public record CategoryPercentageResponse(Category category, BigDecimal totalAmount, BigDecimal percentage) {

    public static CategoryPercentageResponse from(CategoryPercentageOutput output) {
        return new CategoryPercentageResponse(output.category(), output.totalAmount(), output.percentage());
    }
}
