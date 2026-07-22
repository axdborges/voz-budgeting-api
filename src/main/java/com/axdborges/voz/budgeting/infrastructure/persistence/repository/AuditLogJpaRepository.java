package com.axdborges.voz.budgeting.infrastructure.persistence.repository;

import com.axdborges.voz.budgeting.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findAllByOrderByOccurredAtDesc();

    List<AuditLogEntity> findByTransactionIdOrderByOccurredAtDesc(String transactionId);
}
