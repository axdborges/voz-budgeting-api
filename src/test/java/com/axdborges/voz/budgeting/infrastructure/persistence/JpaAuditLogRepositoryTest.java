package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.AuditAction;
import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.infrastructure.persistence.repository.AuditLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaAuditLogRepositoryTest {

    @Autowired
    private AuditLogJpaRepository auditLogJpaRepository;

    private JpaAuditLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaAuditLogRepository(auditLogJpaRepository);
    }

    @Test
    void shouldPersistAndListAllAuditLogsMostRecentFirst() {
        TransactionId id = TransactionId.generate();
        repository.save(new AuditLog(id, AuditAction.CREATED, "criada", LocalDateTime.of(2026, 7, 20, 10, 0)));
        repository.save(new AuditLog(id, AuditAction.UPDATED, "atualizada", LocalDateTime.of(2026, 7, 21, 9, 0)));

        List<AuditLog> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).action()).isEqualTo(AuditAction.UPDATED);
        assertThat(all.get(1).action()).isEqualTo(AuditAction.CREATED);
        assertThat(all.get(0).id()).isNotNull();
    }

    @Test
    void shouldFindOnlyAuditLogsFromTheGivenTransaction() {
        TransactionId trackedId = TransactionId.generate();
        TransactionId otherId = TransactionId.generate();
        repository.save(new AuditLog(trackedId, AuditAction.CREATED, "criada", LocalDateTime.now()));
        repository.save(new AuditLog(otherId, AuditAction.CREATED, "criada", LocalDateTime.now()));

        List<AuditLog> found = repository.findByTransactionId(trackedId);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).transactionId()).isEqualTo(trackedId);
    }

    @Test
    void shouldReturnEmptyListWhenTheTransactionHasNoAuditLogs() {
        assertThat(repository.findByTransactionId(TransactionId.generate())).isEmpty();
    }
}
