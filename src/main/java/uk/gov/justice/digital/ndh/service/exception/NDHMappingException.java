package uk.gov.justice.digital.ndh.service.exception;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
public class NDHMappingException extends RuntimeException {
    private String subject;
    private Long code;
    private String sourceValue;
}
