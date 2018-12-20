package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PhysicalMark {
    private String type;
    private String side;
    private String bodyPart;
    private String orientation;
    private String comment;
    private Long imageId;
}
