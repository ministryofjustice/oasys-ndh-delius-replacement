package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfileDetails {
    private Long profileSeq;
    private String profileType;
    private String profileCode;
    private String profileDescription;
    private Long listSeq;
    private String comments;
    private String caseloadType;
}
