package com.sentinal.contract_risk_auditor.service;

import com.sentinal.contract_risk_auditor.model.AuditFinding;
import com.sentinal.contract_risk_auditor.model.ContractChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContractAuditService {

    private final GeminiService geminiService;

    public ContractAuditService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public List<AuditFinding> auditChunks(List<ContractChunk> chunks) {
        List<AuditFinding> findings = new ArrayList<>();

        for (ContractChunk chunk : chunks) {
            String lowerContent = chunk.content().toLowerCase();

            // Rule 1: Check for Unilateral Termination
            if (lowerContent.contains("terminate") &&
                    (lowerContent.contains("without assigning any reason") || lowerContent.contains("at any time"))) {

                String remedy = geminiService.generateRemedy(
                        chunk.clauseTitle(),
                        chunk.content(),
                        "UNILATERAL_TERMINATION"
                );

                findings.add(new AuditFinding(
                        chunk.chunkIndex(),
                        chunk.clauseTitle(),
                        "UNILATERAL_TERMINATION",
                        "HIGH",
                        "Permits termination at will without cause or explanation.",
                        remedy
                ));
            }

            // Rule 2: Check for Uncapped/One-Sided Indemnification
            if (lowerContent.contains("indemnif")) {
                String remedy = geminiService.generateRemedy(
                        chunk.clauseTitle(),
                        chunk.content(),
                        "INDEMNIFICATION_RISK"
                );

                findings.add(new AuditFinding(
                        chunk.chunkIndex(),
                        chunk.clauseTitle(),
                        "INDEMNIFICATION_RISK",
                        "HIGH",
                        "Contains one-sided indemnification liabilities exposing the provider to broad claims.",
                        remedy
                ));
            }

            // Rule 3: Check for Sole / Restrictive Jurisdiction
            if (lowerContent.contains("sole jurisdiction") || lowerContent.contains("only courts in")) {
                String remedy = geminiService.generateRemedy(
                        chunk.clauseTitle(),
                        chunk.content(),
                        "JURISDICTION_RESTRICTION"
                );

                findings.add(new AuditFinding(
                        chunk.chunkIndex(),
                        chunk.clauseTitle(),
                        "JURISDICTION_RESTRICTION",
                        "MEDIUM",
                        "Exclusive local court jurisdiction limits dispute resolution options.",
                        remedy
                ));
            }
        }

        return findings;
    }
}