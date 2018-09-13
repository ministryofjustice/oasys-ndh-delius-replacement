package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Event {
    @JsonProperty("EventNumber")
    private String eventNumber;
    @JsonProperty("OffenceCode")
    private String offenceCode;
    @JsonProperty("OffenceDate")
    private String offenceDate;
    @JsonProperty("CommencementDate")
    private String commencementDate;
    @JsonProperty("OrderType")
    private String orderType;
    @JsonProperty("OrderLength")
    private String orderLength;
    @JsonProperty("Court")
    private String court;
    @JsonProperty("CourtType")
    private String courtType;
    @JsonProperty("UWHours")
    private String uwHours;
    @JsonProperty("Requirements")
    @JacksonXmlElementWrapper(localName = "Requirements")
    private List<Category> requirements;
    @JsonProperty("AdditionalRequirements")
    @JacksonXmlElementWrapper(localName = "AdditionalRequirements")
    private List<Category> additionalRequirements;
    @JsonProperty("Custody")
    private Custody custody;


}
