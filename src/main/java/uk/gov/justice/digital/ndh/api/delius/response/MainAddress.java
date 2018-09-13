package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class MainAddress {
    @JsonProperty("AddressFirstLine")
    private AddressFirstLine addressFirstLine;
    @JsonProperty("StreetName")
    private String streetName;
    @JsonProperty("District")
    private String district;
    @JsonProperty("TownOrCity")
    private String townOrCity;
    @JsonProperty("County")
    private String county;
    @JsonProperty("TelephoneNumber")
    private String telephoneNumber;

}
