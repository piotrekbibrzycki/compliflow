package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.compliance.AuditEventService;
import com.compliflow.transfer_service.model.AuditEvent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-events")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public List<AuditEvent> getAllAuditEvents() {
        return auditEventService.getAllAuditEvents();
    }

    @GetMapping("/transfer/{transferId}")
    public List<AuditEvent> getAuditEventsByTransferId(@PathVariable Long transferId) {
        return auditEventService.getAuditEventsByTransferId(transferId);
    }
}