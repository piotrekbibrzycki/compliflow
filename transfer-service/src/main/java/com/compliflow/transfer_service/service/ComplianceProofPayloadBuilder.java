package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.Transfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceProofPayloadBuilder {

    private static final String PROOF_SCHEMA_VERSION = "compliflow-proof-v1";

    private final ObjectMapper objectMapper;

    public ComplianceProofPayloadBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getProofSchemaVersion() {
        return PROOF_SCHEMA_VERSION;
    }

    public String buildCanonicalPayload(Transfer transfer, List<AuditEvent> auditEvents) {
        List<AuditEvent> orderedAuditEvents = auditEvents.stream()
                .sorted(Comparator
                        .comparing(AuditEvent::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AuditEvent::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("proofSchemaVersion", PROOF_SCHEMA_VERSION);
        payload.put("transferId", transfer.getId());
        payload.put("transferStatus", enumName(transfer.getStatus()));
        payload.put("complianceDecision", enumName(transfer.getComplianceDecision()));
        payload.put("complianceSummaryReason", blankSafe(transfer.getComplianceReasonSummary()));
        payload.put("createdAt", format(transfer.getCreatedAt()));
        payload.put("completedAt", format(transfer.getCompletedAt()));
        payload.put("reviewMetadata", buildReviewMetadata(transfer));
        payload.put("auditEvents", orderedAuditEvents.stream().map(this::buildAuditEventPayload).toList());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize canonical compliance proof payload", e);
        }
    }

    private Map<String, Object> buildReviewMetadata(Transfer transfer) {
        Map<String, Object> reviewMetadata = new LinkedHashMap<>();
        reviewMetadata.put("reviewedBy", blankSafe(transfer.getReviewedBy()));
        reviewMetadata.put("reviewedAt", format(transfer.getReviewedAt()));
        reviewMetadata.put("reviewComment", blankSafe(transfer.getReviewComment()));
        return reviewMetadata;
    }

    private Map<String, Object> buildAuditEventPayload(AuditEvent auditEvent) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ruleName", blankSafe(auditEvent.getRuleName()));
        payload.put("policyCode", blankSafe(auditEvent.getPolicyCode()));
        payload.put("policyVersion", blankSafe(auditEvent.getPolicyVersion()));
        payload.put("scenarioCode", enumName(auditEvent.getScenarioCode()));
        payload.put("decision", enumName(auditEvent.getDecision()));
        payload.put("reason", blankSafe(auditEvent.getReason()));
        payload.put("legalContext", blankSafe(auditEvent.getLegalContext()));
        payload.put("internalPolicy", blankSafe(auditEvent.getInternalPolicy()));
        payload.put("userFacingExplanation", blankSafe(auditEvent.getUserFacingExplanation()));
        payload.put("metadataJson", blankSafe(auditEvent.getMetadataJson()));
        payload.put("reviewedBy", blankSafe(auditEvent.getReviewedBy()));
        payload.put("createdAt", format(auditEvent.getCreatedAt()));
        return payload;
    }

    private String blankSafe(String value) {
        return value == null ? "" : value;
    }

    private String enumName(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }
}