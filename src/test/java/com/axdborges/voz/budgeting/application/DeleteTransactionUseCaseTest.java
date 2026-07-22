package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldDeleteTheTransactionWhenItExists() {
        TransactionId id = TransactionId.generate();
        Transaction transaction = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.valueOf(50),
                LocalDateTime.now());
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        var useCase = new DeleteTransactionUseCase(transactionRepository, auditLogRepository);

        useCase.execute(id);

        verify(transactionRepository).deleteById(id);
    }

    @Test
    void shouldThrowAndNotDeleteWhenTheTransactionDoesNotExist() {
        TransactionId id = TransactionId.generate();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        var useCase = new DeleteTransactionUseCase(transactionRepository, auditLogRepository);

        assertThatThrownBy(() -> useCase.execute(id)).isInstanceOf(TransactionNotFoundException.class);
        verify(transactionRepository, never()).deleteById(id);
    }

    @Test
    void shouldRecordAnAuditLogWithTheDeletedActionAndASnapshotOfTheTransaction() {
        TransactionId id = TransactionId.generate();
        Transaction transaction = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.valueOf(50),
                LocalDateTime.now());
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        var useCase = new DeleteTransactionUseCase(transactionRepository, auditLogRepository);

        useCase.execute(id);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertThat(captor.getValue().action()).isEqualTo(AuditAction.DELETED);
        assertThat(captor.getValue().detail()).contains("MERCADO").contains("50").contains("supermercado");
    }
}
