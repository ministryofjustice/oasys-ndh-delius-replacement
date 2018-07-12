package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskType {

//    	<xs:element name="RiskType">
//		<xs:complexType>
//			<xs:sequence>
//				<xs:element name="CaseReferenceNumber" type="cmn:CaseReferenceNumberType">
    @JsonProperty("CaseReferenceNumber")
    private String caseReferenceNumber;
//					<xs:annotation>
//						<xs:appinfo>Offender.CRN</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="RiskOfHarm">

    @JsonProperty("RiskOfHarm")
    private String riskOfHarm;
//					<xs:annotation>
//						<xs:appinfo>Offender.RiskAssessment.RiskOfHarm.Code</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:minLength value="1"/>
//							<xs:maxLength value="20"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//			</xs:sequence>
//		</xs:complexType>
//	</xs:element>
}
