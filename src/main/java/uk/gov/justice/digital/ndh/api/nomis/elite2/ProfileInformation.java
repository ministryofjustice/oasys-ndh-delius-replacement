package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ProfileInformation {
    private String type;
    private String question;
    private String resultValue;
}
