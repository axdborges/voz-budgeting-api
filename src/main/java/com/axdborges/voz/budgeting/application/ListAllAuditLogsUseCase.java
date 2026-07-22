package com.axdborges.voz.budgeting.application;

import com.axdborges.voz.budgeting.application.output.AuditLogOutput;
import com.axdborges.voz.budgeting.domain.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAllAuditLogsUseCase {

    private final AuditLogRepository auditLogRepository;

    public ListAllAuditLogsUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogOutput> execute() {
        return auditLogRepository.findAll().stream().map(AuditLogOutput::from).toList();
    }
}
