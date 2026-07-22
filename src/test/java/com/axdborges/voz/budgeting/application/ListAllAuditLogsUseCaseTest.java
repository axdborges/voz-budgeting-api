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
class ListAllAuditLogsUseCaseTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldReturnAllAuditLogsFromTheRepositoryAsOutput() {
        AuditLog created = new AuditLog(1L, TransactionId.generate(), AuditAction.CREATED, "detalhe",
                LocalDateTime.now());
        AuditLog deleted = new AuditLog(2L, TransactionId.generate(), AuditAction.DELETED, "detalhe",
                LocalDateTime.now());
        when(auditLogRepository.findAll()).thenReturn(List.of(created, deleted));
        var useCase = new ListAllAuditLogsUseCase(auditLogRepository);

        List<AuditLogOutput> outputs = useCase.execute();

        assertThat(outputs).hasSize(2);
        assertThat(outputs).extracting(AuditLogOutput::action)
                .containsExactlyInAnyOrder(AuditAction.CREATED, AuditAction.DELETED);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAuditLogs() {
        when(auditLogRepository.findAll()).thenReturn(List.of());
        var useCase = new ListAllAuditLogsUseCase(auditLogRepository);

        assertThat(useCase.execute()).isEmpty();
    }
}
