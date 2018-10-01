package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdentifyingMark {
    private Long idMarkSeq;
    private String bodyPartCode;
    private String markType;
    private String sideCode;
    private String partOrientationCode;
    private String comments;
}
