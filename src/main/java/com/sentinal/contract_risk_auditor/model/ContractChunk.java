package com.sentinal.contract_risk_auditor.model;

public record ContractChunk(
        int chunkIndex,
        String clauseTitle,
        String content
) {}