package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.ListAllAuditLogsUseCase;
import com.axdborges.voz.budgeting.application.ListAuditLogsByTransactionUseCase;
import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.infrastructure.http.response.AuditLogResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final ListAllAuditLogsUseCase listAllAuditLogsUseCase;
    private final ListAuditLogsByTransactionUseCase listAuditLogsByTransactionUseCase;

    public AuditLogController(ListAllAuditLogsUseCase listAllAuditLogsUseCase,
                               ListAuditLogsByTransactionUseCase listAuditLogsByTransactionUseCase) {
        this.listAllAuditLogsUseCase = listAllAuditLogsUseCase;
        this.listAuditLogsByTransactionUseCase = listAuditLogsByTransactionUseCase;
    }

    @GetMapping
    public List<AuditLogResponse> list(@RequestParam(required = false) String transactionId) {
        List<AuditLogOutput> outputs = transactionId != null
                ? listAuditLogsByTransactionUseCase.execute(TransactionId.of(transactionId))
                : listAllAuditLogsUseCase.execute();

        return outputs.stream().map(AuditLogResponse::from).toList();
    }
}
