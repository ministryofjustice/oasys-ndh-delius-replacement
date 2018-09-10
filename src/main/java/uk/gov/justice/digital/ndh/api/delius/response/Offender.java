package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Offender {

    @JsonProperty("LAOIndicator")
    private String laoIndicator;
    @JsonProperty("CaseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("PoliceNationalComputerIdentifier")
    private String policeNationalComputerIdentifier;
    @JsonProperty("LastName")
    private String lastName;
    @JsonProperty("Forename1")
    private String forename1;
    @JsonProperty("Forename2")
    private String forename2;
    @JsonProperty("Forename3")
    private String forename3;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("Alias")
    private List<String> aliases;
    @JsonProperty("Gender")
    private String gender;
    @JsonProperty("DateOfBirth")
    private String dateOfBirth;
    @JsonProperty("Language")
    private String language;
    @JsonProperty("Religion")
    private String religion;
    @JsonProperty("CRO")
    private String cro;
    @JsonProperty("Ethnicity")
    private String ethnicity;
    @JsonProperty("MainAddress")
    private MainAddress mainAddress;
    @JsonProperty("PrisonNumber")
    private String prisonNumber;
    @JsonProperty("Postcode")
    private String postcode;
    @JsonProperty("Telephone")
    private String telephone;


}
