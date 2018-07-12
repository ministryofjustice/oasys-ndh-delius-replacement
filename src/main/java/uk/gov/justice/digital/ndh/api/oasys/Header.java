package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class Header {

    /*
        <dom:ApplicationMode>I</dom:ApplicationMode>
        <dom:CorrelationID>OASYSRPCWWS20180620000647594588</dom:CorrelationID>
        <dom:OASysRUsername/>
        <dom:MessageTimestamp>2018-06-20T00:01:47+01:00</dom:MessageTimestamp>

 */

    @JsonProperty("ApplicationMode")
    private String applicationMode;

    @JsonProperty("CorrelationID")
    private String correlationID;

    @JsonProperty("OASysRUsername")
    private String oasysRUsername;

    @JsonProperty("MessageTimestamp")
    private OffsetDateTime messageTimestamp;

}
