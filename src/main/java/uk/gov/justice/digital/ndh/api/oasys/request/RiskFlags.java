package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
public class RiskFlags {

    //  <xs:element name="R10Q6a" type="xs:string"/>
    @JsonProperty("R10Q6a")
    private String r10Q6a;
    //	<xs:element name="R10Q6b" type="xs:string"/>
    @JsonProperty("R10Q6b")
    private String r10Q6b;
    //	<xs:element name="R10Q6c" type="xs:string"/>
    @JsonProperty("R10Q6c")
    private String r10Q6c;
    //	<xs:element name="R10Q6d" type="xs:string"/>
    @JsonProperty("R10Q6d")
    private String r10Q6d;
    //	<xs:element name="R10Q6e" type="xs:string"/>
    @JsonProperty("R10Q6e")
    private String r10Q6e;
    //	<xs:element name="R10Q6f" type="xs:string"/>
    @JsonProperty("R10Q6f")
    private String r10Q6f;
    //	<xs:element name="R10Q6g" type="xs:string"/>
    @JsonProperty("R10Q6g")
    private String r10Q6g;
    //	<xs:element name="R10Q6h" type="xs:string"/>
    @JsonProperty("R10Q6h")
    private String r10Q6h;
    //	<xs:element name="R10Q6i" type="xs:string"/>
    @JsonProperty("R10Q6i")
    private String r10Q6i;
}
