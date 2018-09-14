package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyValue {
    private String code;
    private String description;
}
