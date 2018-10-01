package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class Identifier {
    private String identifierType;
    private String identifier;
    private Long sequenceNumber;
    private LocalDateTime createdDateTime;
}
