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
    private String value;

    @Override
    public String getMessage() {
        return String.format("Failed when mapping PCMS Response in NDH: Could not resolve subject %s from value %s and code %s ", subject, value, code);
    }
}
