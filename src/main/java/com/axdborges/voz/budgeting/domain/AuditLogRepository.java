package com.axdborges.voz.budgeting.domain;

import java.util.List;

public interface AuditLogRepository {

    void save(AuditLog auditLog);

    List<AuditLog> findAll();

    List<AuditLog> findByTransactionId(TransactionId transactionId);
}
