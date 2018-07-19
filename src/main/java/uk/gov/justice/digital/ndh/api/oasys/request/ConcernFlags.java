package uk.gov.justice.digital.ndh.api.oasys.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConcernFlags {
    //  <xs:element name="R8Q11SelfHarm" type="xs:string"/>
    @JsonProperty("R8Q11SelfHarm")
    private String r8q11SelfHarm;
    //	<xs:element name="R8Q21Custody" type="xs:string"/>
    @JsonProperty("R8Q21Custody")
    private String r8q21Custody;
    //	<xs:element name="R8Q21Hostel" type="xs:string"/>
    @JsonProperty("R8Q21Hostel")
    private String r8q21Hostel;
    //	<xs:element name="R8Q31" type="xs:string"/>
    @JsonProperty("R8Q31")
    private String r8q31;
    //	<xs:element name="R9Q11" type="xs:string"/>
    @JsonProperty("R9Q11")
    private String r9q11;
    //	<xs:element name="R9Q21" type="xs:string"/>
    @JsonProperty("R9Q21")
    private String r9q21;
    //	<xs:element name="R9Q31" type="xs:string"/>
    @JsonProperty("R9Q31")
    private String r9q31;
    //	<xs:element name="R8Q11Suicide" type="xs:string"/>
    @JsonProperty("R8Q11Suicide")
    private String r8q11Suicide;

}
