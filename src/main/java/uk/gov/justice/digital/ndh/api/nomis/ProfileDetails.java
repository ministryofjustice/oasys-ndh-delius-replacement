package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

@Data
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
