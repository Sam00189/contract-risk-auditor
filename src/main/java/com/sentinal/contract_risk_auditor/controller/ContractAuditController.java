package com.sentinal.contract_risk_auditor.controller;

import com.sentinal.contract_risk_auditor.model.AuditFinding;
import com.sentinal.contract_risk_auditor.model.ContractChunk;
import com.sentinal.contract_risk_auditor.service.ContractAuditService;
import com.sentinal.contract_risk_auditor.service.PdfParserService;
import com.sentinal.contract_risk_auditor.service.TextChunkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractAuditController {

    private final PdfParserService pdfParserService;
    private final TextChunkerService textChunkerService;
    private final ContractAuditService contractAuditService;

    public ContractAuditController(
            PdfParserService pdfParserService,
            TextChunkerService textChunkerService,
            ContractAuditService contractAuditService
    ) {
        this.pdfParserService = pdfParserService;
        this.textChunkerService = textChunkerService;
        this.contractAuditService = contractAuditService;
    }

    @PostMapping("/parse")
    public ResponseEntity<String> parseContractPdf(@RequestParam("file") MultipartFile file) throws IOException {
        String extractedText = pdfParserService.extractText(file);
        return ResponseEntity.ok(extractedText);
    }

    @PostMapping("/chunk")
    public ResponseEntity<List<ContractChunk>> chunkContractPdf(@RequestParam("file") MultipartFile file) throws IOException {
        String extractedText = pdfParserService.extractText(file);
        List<ContractChunk> chunks = textChunkerService.chunkContract(extractedText);
        return ResponseEntity.ok(chunks);
    }

    @PostMapping("/audit")
    public ResponseEntity<List<AuditFinding>> auditContractPdf(@RequestParam("file") MultipartFile file) throws IOException {
        String extractedText = pdfParserService.extractText(file);
        List<ContractChunk> chunks = textChunkerService.chunkContract(extractedText);
        List<AuditFinding> findings = contractAuditService.auditContract(chunks);
        return ResponseEntity.ok(findings);
    }
}