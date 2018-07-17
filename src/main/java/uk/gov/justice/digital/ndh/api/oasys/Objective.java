package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Objective {

    //            <xs:element name="ObjectiveDescription">
    @JsonProperty("ObjectiveDescription")
    private String objectiveDescription;
    //            <xs:element name="ObjectiveStatus">
    @JsonProperty("ObjectiveStatus")
    private String objectiveStatus;
    //            <xs:element name="ObjectiveNumber" minOccurs="0">
    @JsonProperty("ObjectiveNumber")
    private String objectiveNumber;
    //            <xs:element name="Need1" minOccurs="0">
    @JsonProperty("Need1")
    private String need1;
    //            <xs:element name="Need2" minOccurs="0">
    @JsonProperty("Need2")
    private String need2;
    //            <xs:element name="Need3" minOccurs="0">
    @JsonProperty("Need3")
    private String need3;
    //            <xs:element name="Need4" minOccurs="0">
    @JsonProperty("Need4")
    private String need4;
    //            <xs:element name="ActionCode1" minOccurs="0">
    @JsonProperty("ActionCode1")
    private String actionCode1;
    //            <xs:element name="ActionText1" minOccurs="0">
    @JsonProperty("ActionText1")
    private String actionText1;
    //            <xs:element name="ActionCode2" minOccurs="0">
    @JsonProperty("ActionCode2")
    private String actionCode2;
    //            <xs:element name="ActionText2" minOccurs="0">
    @JsonProperty("ActionText2")
    private String actionText2;
    //            <xs:element name="ActionCode3" minOccurs="0">
    @JsonProperty("ActionCode3")
    private String actionCode3;
    //            <xs:element name="ActionText3" minOccurs="0">
    @JsonProperty("ActionText3")
    private String actionText3;

}
