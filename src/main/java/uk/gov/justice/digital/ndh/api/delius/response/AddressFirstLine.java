package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AddressFirstLine {
    @JsonProperty("BuildingName")
    private String buildingName;
    @JsonProperty("HouseNumber")
    private String houseNumber;
}
