package uk.gov.justice.digital.ndh.service.exception;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
public class NDHRequirementLookupException extends RuntimeException {
    private String reqType;
    private String reqCode;
    private String subCode;

    @Override
    public String getMessage() {
        return String.format("Failed when mapping PCMS Response in NDH: Could not resolve REQUIREMNT_LOOKUP from reqType %s and reqCode %s and subCode %s", reqType, reqCode, subCode);
    }
}
