package com.sentinal.contract_risk_auditor.model;

public record AuditFinding(
        int chunkIndex,
        String clauseTitle,
        String riskCategory,       // e.g., "TERMINATION", "INDEMNITY", "JURISDICTION"
        String severity,           // "HIGH", "MEDIUM", "LOW"
        String explanation,        // Why this specific clause is dangerous
        String suggestedRemedy     // How the clause should be amended
) {}