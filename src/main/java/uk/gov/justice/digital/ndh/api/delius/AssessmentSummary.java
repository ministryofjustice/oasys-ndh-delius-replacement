package uk.gov.justice.digital.ndh.api.delius;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentSummary {

//    <xs:element name="OASYSAssessmentSummary">
//		<xs:complexType>
//			<xs:sequence>
//				<xs:element name="CaseReferenceNumber" type="xs:string">
//					<xs:annotation>
//						<xs:appinfo>Offender.CRN</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="EventNumber" type="cmn:EventNumberType">
//					<xs:annotation>
//						<xs:appinfo>Offender.Event.EventNumber</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="DateAssessmentCompleted" type="xs:date" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.AssessmentDate</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection2Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection2Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection3Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection3Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection4Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection4Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection5Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection5Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASysSection6Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection6Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection7Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection7Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection8Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection8Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection9Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection9Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection10Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection10Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection11Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection11Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection12Scores" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection12Scores</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection3Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection3Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection4Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection4Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection6Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection6Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection7Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection7Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection8Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection8Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection9Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection9Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection11Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection11Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSSection12Score" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASYSSection12Score</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="ConcernFlags" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.ConcernFlags</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="50"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="OASYS_ID" type="cmn:OASYS_ID_Type">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OASysID</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OASYSTotalScore" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:documentation>Offender.OASYSAssessment.TotalScore</xs:documentation>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="PurposeOfAssessmentCode" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.AssessmentPurposeCode</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="20"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="PurposeOfAssessmentDescription" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.AssessmentPurposeDescription</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="50"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="DateCreated" type="xs:date">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.DateCreated</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="AssessedBy" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.AssessedBy</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="100"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="Court" type="cmn:CourtNameType" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.Court</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="CourtType" type="cmn:CourtType" minOccurs="0"/>
//				<xs:element name="OffenceCode" type="cmn:OffenderCodeType" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OffenceCode</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OGRSScore1" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OGRSScore1</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OGRSScore2" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OGRSScore2</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OGPNotCalculated" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OGPNotCalculated</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:length value="1"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="OVPNotCalculated" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OVPNotCalculated</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:length value="1"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="OGPScore1" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OGPScore1</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OGPScore2" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OGPScore2</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OVPScore1" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OVPScore1</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OVPScore2" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>
//							<xs:annotation>
//								<xs:appinfo>Offender.OASYSAssessment.OVPScore2</xs:appinfo>
//							</xs:annotation>
//						</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="OGRSRiskRecon" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OGRSRiskRecon</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="10"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="OGPRiskRecon" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OGPRiskRecon</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="10"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="OVPRiskRecon" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.OVPRiskRecon</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="10"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="TierCode" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.TierCode</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="50"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="Layer1Obj" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.Layer1Obj</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="20"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="SentencePlanReviewDate" type="xs:date" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.SentencePlanReviewDate</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="SentencePlanInitialDate" type="xs:date" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.SentencePlanInitialDate</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="ReviewTerminated" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.ReviewTerminated</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="1"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//				<xs:element name="ReviewNumber" type="xs:integer" minOccurs="0">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.ReviewNumber</xs:appinfo>
//					</xs:annotation>
//				</xs:element>
//				<xs:element name="LayerType">
//					<xs:annotation>
//						<xs:appinfo>Offender.OASYSAssessment.LayerType</xs:appinfo>
//					</xs:annotation>
//					<xs:simpleType>
//						<xs:restriction base="xs:string">
//							<xs:maxLength value="20"/>
//						</xs:restriction>
//					</xs:simpleType>
//				</xs:element>
//			</xs:sequence>
//		</xs:complexType>
//	</xs:element>
}
