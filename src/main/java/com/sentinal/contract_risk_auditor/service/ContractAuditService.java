package com.sentinal.contract_risk_auditor.service;

import com.sentinal.contract_risk_auditor.model.AuditFinding;
import com.sentinal.contract_risk_auditor.model.ContractChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContractAuditService {

    private final GeminiService geminiService;

    public ContractAuditService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    private static final List<RiskRule> RULES = List.of(
            // 1. Broad Indemnity
            new RiskRule(
                    Pattern.compile("(?i)\\bindemnif(y|ies|ication)\\b"),
                    "INDEMNIFICATION_RISK",
                    "HIGH",
                    "Contains broad indemnification liabilities exposing the party to severe claims."
            ),
            // 2. Unilateral Termination
            new RiskRule(
                    Pattern.compile("(?i)\\b(terminate without cause|terminate at (its )?sole discretion|terminate at will|waive all or part of the notice period)\\b"),
                    "UNILATERAL_TERMINATION",
                    "HIGH",
                    "Permits discretionary or unbalanced termination without sufficient reciprocal notice or cure rights."
            ),
            // 3. Jurisdiction Restrictions
            new RiskRule(
                    Pattern.compile("(?i)\\b(exclusive jurisdiction|courts at Kanpur|courts of)\\b"),
                    "JURISDICTION_RESTRICTION",
                    "MEDIUM",
                    "Exclusive jurisdiction restricts dispute resolution venue options."
            ),
            // 4. Post-Termination Non-Compete (Restraint of Trade)
            new RiskRule(
                    Pattern.compile("(?i)\\b(shall not.*(provide.*similar services|compete)|non-compete|restrictive covenants|following termination.*direct competitor)\\b"),
                    "RESTRAINT_OF_TRADE",
                    "HIGH",
                    "Imposes post-termination non-compete covenants which are often unenforceable or void under local labor laws (e.g., Section 27 of the Indian Contract Act)."
            ),
            // 5. Unilateral Deductions & Penalties
            new RiskRule(
                    Pattern.compile("(?i)\\b(deductions or adjustments from salary|recover.*training expenses|losses, penalties)\\b"),
                    "COMPENSATION_DEDUCTION_RISK",
                    "MEDIUM",
                    "Authorizes discretionary salary deductions, expense clawbacks, or employer-assessed penalties without judicial process."
            ),
            // 6. Overbroad IP Assignment
            new RiskRule(
                    Pattern.compile("(?i)\\b(treating the material as company-owned|pre-existing inventions|belong exclusively to the company to the fullest extent)\\b"),
                    "OVERBROAD_IP_ASSIGNMENT",
                    "MEDIUM",
                    "Claims ownership over employee pre-existing IP, personal inventions, or materials developed outside employer scope."
            )
    );

    public List<AuditFinding> auditContract(List<ContractChunk> chunks) {
        List<AuditFinding> findings = new ArrayList<>();

        for (ContractChunk chunk : chunks) {
            for (RiskRule rule : RULES) {
                if (rule.pattern().matcher(chunk.content()).find()) {
                    // Calls generateRemedy(String clauseTitle, String clauseContent, String riskCategory)
                    String remedy = geminiService.generateRemedy(
                            chunk.clauseTitle(),
                            chunk.content(),
                            rule.category()
                    );

                    findings.add(new AuditFinding(
                            chunk.chunkIndex(),
                            chunk.clauseTitle(),
                            rule.category(),
                            rule.severity(),
                            rule.explanation(),
                            remedy
                    ));
                    break;
                }
            }
        }
        return findings;
    }

    private record RiskRule(Pattern pattern, String category, String severity, String explanation) {}
}