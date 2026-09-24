package com.sentinal.contract_risk_auditor.service;

import com.sentinal.contract_risk_auditor.model.ContractChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    public List<ContractChunk> chunkContract(String fullText) {
        List<ContractChunk> chunks = new ArrayList<>();

        if (fullText == null || fullText.isBlank()) {
            return chunks;
        }

        // 1. Split the entire document into individual lines
        String[] lines = fullText.split("\\r?\\n");

        StringBuilder currentClauseBody = new StringBuilder();
        String currentTitle = "PREAMBLE";
        int chunkCount = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue; // Skip blank lines
            }

            // 2. Check: Does this line look like a new clause or section header?
            if (isHeading(line)) {
                // If we already collected text for the previous clause, package it!
                if (!currentClauseBody.isEmpty()) {
                    chunks.add(new ContractChunk(chunkCount++, currentTitle, currentClauseBody.toString().trim()));
                    currentClauseBody.setLength(0); // reset buffer for the next clause
                }
                currentTitle = line; // New title
            }

            // 3. Add this line into the active clause body
            currentClauseBody.append(line).append(" ");
        }

        // 4. Save the very last clause remaining in the buffer
        if (!currentClauseBody.isEmpty()) {
            chunks.add(new ContractChunk(chunkCount, currentTitle, currentClauseBody.toString().trim()));
        }

        return chunks;
    }

    /**
     * Checks if a line is likely a contract heading:
     * - Starts with a number (e.g., "1. SCOPE", "3.2 Fees")
     * - Starts with words like "ARTICLE", "SECTION", "CLAUSE"
     * - Or is short and written in ALL CAPS (e.g., "TERMINATION")
     */
    private boolean isHeading(String line) {
        // Starts with "1." or "1.1" or "IV."
        if (line.matches("^((\\d+(\\.\\d+)*\\.?)|([IVXLCDM]+\\.))\\s+.*")) {
            return true;
        }

        // Starts with common legal labels: "SECTION", "ARTICLE", "CLAUSE"
        String upper = line.toUpperCase();
        if (upper.startsWith("SECTION") || upper.startsWith("ARTICLE") || upper.startsWith("CLAUSE")) {
            return true;
        }

        // Short lines that are purely UPPERCASE words (e.g., "GOVERNING LAW")
        if (line.length() < 50 && line.equals(upper) && line.matches(".*[A-Z]{3,}.*")) {
            return true;
        }

        return false;
    }
}