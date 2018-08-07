package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubSetOffender {
    @JsonProperty("LAOIndicator")
    private String laoIndicator;
    @JsonProperty("CaseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("LastName")
    private String lastName;
    @JsonProperty("Forename1")
    private String forename1;
    @JsonProperty("DateOfBirth")
    private String dateOfBirth;
    @JsonProperty("Gender")
    private String gender;
}
