package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PhysicalCharacteristic {
    private String type;
    private String characteristic;
    private String detail;
    private Long imageId;
}
