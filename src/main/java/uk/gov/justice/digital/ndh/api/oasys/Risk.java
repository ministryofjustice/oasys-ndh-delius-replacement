package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Risk {
    //           <xs:element name="RiskofHarm" type="dt:ElementCode"/>
    @JsonProperty("RiskofHarm")
    private String RiskofHarm;
}
