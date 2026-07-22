package com.axdborges.voz.budgeting.application.output;

import java.math.BigDecimal;
import java.util.List;

public record SpendingReportOutput(BigDecimal totalAmount, List<CategoryPercentageOutput> categories) {
}
