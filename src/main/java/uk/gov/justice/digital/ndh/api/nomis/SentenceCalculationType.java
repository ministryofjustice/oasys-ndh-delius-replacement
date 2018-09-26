package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SentenceCalculationType {
    private String sentenceCategory;
    private String sentenceCalculationType;
    private String sentenceType;
    private String description;
    private LocalDateTime expiryDate;
    private Boolean active;
    private String ProgramMethod;
    private String functionType;
}
