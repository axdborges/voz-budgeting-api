package com.axdborges.voz.budgeting.infrastructure.http.response;

import com.axdborges.voz.budgeting.application.output.SpendingReportOutput;

import java.math.BigDecimal;
import java.util.List;

public record ReportResponse(String report, BigDecimal totalAmount, List<CategoryPercentageResponse> categories) {

    public static ReportResponse from(String report, SpendingReportOutput output) {
        List<CategoryPercentageResponse> categories = output.categories().stream()
                .map(CategoryPercentageResponse::from)
                .toList();
        return new ReportResponse(report, output.totalAmount(), categories);
    }
}
