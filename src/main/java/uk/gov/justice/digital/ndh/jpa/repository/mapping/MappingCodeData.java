package uk.gov.justice.digital.ndh.jpa.repository.mapping;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MappingCodeData {
    private Long codeType;
    private String sourceValue;
    private String targetValue;
    private String description;
    private Long numcode;
    private Long rank;
    private Long numeric1;

    public MappingCodeKey getKey() {
        return MappingCodeKey.builder().codeType(codeType).sourceValue(sourceValue).build();
    }

}
