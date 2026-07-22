package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.TransactionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAuditLogsByTransactionUseCaseTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldReturnOnlyTheAuditLogsFromTheGivenTransaction() {
        TransactionId id = TransactionId.generate();
        AuditLog created = new AuditLog(1L, id, AuditAction.CREATED, "detalhe", LocalDateTime.now());
        AuditLog updated = new AuditLog(2L, id, AuditAction.UPDATED, "detalhe", LocalDateTime.now());
        when(auditLogRepository.findByTransactionId(id)).thenReturn(List.of(created, updated));
        var useCase = new ListAuditLogsByTransactionUseCase(auditLogRepository);

        List<AuditLogOutput> outputs = useCase.execute(id);

        assertThat(outputs).hasSize(2);
        assertThat(outputs).extracting(AuditLogOutput::transactionId)
                .containsOnly(id.value().toString());
    }

    @Test
    void shouldReturnEmptyListWhenTheTransactionHasNoAuditLogs() {
        TransactionId id = TransactionId.generate();
        when(auditLogRepository.findByTransactionId(id)).thenReturn(List.of());
        var useCase = new ListAuditLogsByTransactionUseCase(auditLogRepository);

        assertThat(useCase.execute(id)).isEmpty();
    }
}
