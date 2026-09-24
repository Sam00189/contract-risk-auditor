package com.sentinal.contract_risk_auditor.service;

import com.sentinal.contract_risk_auditor.model.ContractChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextChunkerService {

    // Matches numbered sections like "1. Appointment", "2. Term", etc.
    private static final Pattern CLAUSE_PATTERN = Pattern.compile("(?m)^(\\d+\\.\\s+[^\\n\\r]+)");

    public List<ContractChunk> chunkContract(String fullText) {
        List<ContractChunk> chunks = new ArrayList<>();
        Matcher matcher = CLAUSE_PATTERN.matcher(fullText);

        List<Integer> splitIndices = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        while (matcher.find()) {
            splitIndices.add(matcher.start());
            titles.add(matcher.group(1).trim());
        }

        if (splitIndices.isEmpty()) {
            chunks.add(new ContractChunk(1, "Full Agreement", fullText.trim()));
            return chunks;
        }

        for (int i = 0; i < splitIndices.size(); i++) {
            int start = splitIndices.get(i);
            int end = (i + 1 < splitIndices.size()) ? splitIndices.get(i + 1) : fullText.length();
            String chunkContent = fullText.substring(start, end).trim();

            chunks.add(new ContractChunk(i + 1, titles.get(i), chunkContent));
        }

        return chunks;
    }
}