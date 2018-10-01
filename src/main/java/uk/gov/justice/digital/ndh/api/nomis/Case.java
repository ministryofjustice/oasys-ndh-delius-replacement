package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class Case {
    private Long caseId;
    private String caseInfoNumber;
    private String caseType;
    private String caseStatus;
    private Long combinedCaseId;
    private LocalDate beginDate;
    private String agencyLocationId;
    private String victimLiasonUnit;
    private Integer lidsCaseNumber;
    private Integer nomLegalCaseRef;
    private Integer nomLegalCaseRefTransTo;
    private Integer caseSequence;
}
