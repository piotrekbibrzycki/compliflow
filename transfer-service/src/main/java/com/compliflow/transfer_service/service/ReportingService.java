package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.dto.ComplianceSummaryPolicyMetricDto;
import com.compliflow.transfer_service.dto.ComplianceSummaryReportResponseDto;
import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.repository.AuditEventRepository;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private final TransferRepository transferRepository;
    private final AuditEventRepository auditEventRepository;

    public ReportingService(TransferRepository transferRepository, AuditEventRepository auditEventRepository) {
        this.transferRepository = transferRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(readOnly = true)
    public ComplianceSummaryReportResponseDto getComplianceSummary() {
        List<Transfer> transfers = transferRepository.findAll();
        List<AuditEvent> auditEvents = auditEventRepository.findAll();

        Map<String, Long> decisionBreakdown = new LinkedHashMap<>();
        for (ComplianceDecision decision : ComplianceDecision.values()) {
            decisionBreakdown.put(decision.name(), transfers.stream()
                    .filter(transfer -> transfer.getComplianceDecision() == decision)
                    .count());
        }

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (TransferStatus status : TransferStatus.values()) {
            statusBreakdown.put(status.name(), transfers.stream()
                    .filter(transfer -> transfer.getStatus() == status)
                    .count());
        }

        List<ComplianceSummaryPolicyMetricDto> topPolicyHits = auditEvents.stream()
                .filter(event -> event.getPolicyCode() != null && !event.getPolicyCode().isBlank())
                .collect(Collectors.groupingBy(
                        event -> new PolicyMetricKey(
                                event.getPolicyCode(),
                                event.getPolicyVersion(),
                                event.getScenarioCode(),
                                event.getDecision()
                        ),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> ComplianceSummaryPolicyMetricDto.builder()
                        .policyCode(entry.getKey().policyCode())
                        .policyVersion(entry.getKey().policyVersion())
                        .scenarioCode(entry.getKey().scenarioCode())
                        .decision(entry.getKey().decision())
                        .hitCount(entry.getValue())
                        .build())
                .sorted(Comparator.comparingLong(ComplianceSummaryPolicyMetricDto::getHitCount).reversed()
                        .thenComparing(ComplianceSummaryPolicyMetricDto::getPolicyCode, Comparator.nullsLast(String::compareTo)))
                .limit(10)
                .toList();

        return ComplianceSummaryReportResponseDto.builder()
                .decisionBreakdown(decisionBreakdown)
                .statusBreakdown(statusBreakdown)
                .totalAuditEvents(auditEvents.size())
                .totalAnchoredProofs(auditEventRepository.countDistinctAnchoredTransferIds())
                .topPolicyHits(topPolicyHits)
                .build();
    }

    private record PolicyMetricKey(
            String policyCode,
            String policyVersion,
            com.compliflow.transfer_service.model.PolicyScenarioCode scenarioCode,
            ComplianceDecision decision
    ) {
    }
}