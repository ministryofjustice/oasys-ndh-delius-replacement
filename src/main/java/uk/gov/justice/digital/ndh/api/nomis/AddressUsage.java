package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressUsage {
    private KeyValue usage;
    private Boolean active;
}
