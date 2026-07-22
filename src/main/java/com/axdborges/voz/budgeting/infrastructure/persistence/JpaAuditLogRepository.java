package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.AuditLog;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.infrastructure.persistence.entity.AuditLogEntity;
import com.axdborges.voz.budgeting.infrastructure.persistence.repository.AuditLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAuditLogRepository implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    public JpaAuditLogRepository(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(AuditLog auditLog) {
        jpaRepository.save(toEntity(auditLog));
    }

    @Override
    public List<AuditLog> findAll() {
        return jpaRepository.findAllByOrderByOccurredAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByTransactionId(TransactionId transactionId) {
        return jpaRepository.findByTransactionIdOrderByOccurredAtDesc(transactionId.value().toString()).stream()
                .map(this::toDomain)
                .toList();
    }

    private AuditLogEntity toEntity(AuditLog auditLog) {
        return new AuditLogEntity(auditLog.transactionId().value().toString(), auditLog.action(),
                auditLog.detail(), auditLog.occurredAt());
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        return new AuditLog(entity.getId(), TransactionId.of(entity.getTransactionId()), entity.getAction(),
                entity.getDetail(), entity.getOccurredAt());
    }
}
