package uk.gov.justice.digital.ndh.jpa.repository.mapping;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MappingCodeKey {
    private Long codeType;
    private String sourceValue;

}
