package uk.gov.justice.digital.ndh.api.delius;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import uk.gov.justice.digital.ndh.api.delius.request.Header;
import uk.gov.justice.digital.ndh.api.delius.request.OasysAssessmentSummary;
import uk.gov.justice.digital.ndh.api.delius.request.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class DeliusAssessmentUpdateSoapEnvelopeTest {

    @Test
    public void builtMessageSerializesToValidSoapMessage() throws IOException {

        final SoapEnvelopeSpec1_2 builtMessage = SoapEnvelopeSpec1_2.builder()
                .header(SoapHeader
                        .builder()
                        .header(Header
                                .builder()
                                .messageId("1234567890123456789012345678901")
                                .version("?")
                                .build())
                        .build())
                .body(SoapBody
                        .builder()
                        .submitAssessmentSummaryRequest(SubmitAssessmentSummaryRequest
                                .builder()
                                .oasysSupervisionPlans(ImmutableList.of(OasysSupervisionPlan
                                        .builder()
                                        .oasysId("?")
                                        .caseReferenceNumber("?")
                                        .need1("?")
                                        .need2("?")
                                        .need3("?")
                                        .need4("?")
                                        .objective("?")
                                        .objectiveNumber("1")
                                        .objectiveStatus("?")
                                        .text1("?")
                                        .text2("?")
                                        .text3("?")
                                        .workSummary1("?")
                                        .workSummary2("?")
                                        .workSummary3("?")
                                        .build()))
                                .riskType(RiskType
                                        .builder()
                                        .caseReferenceNumber("?")
                                        .riskOfHarm("?")
                                        .build())
                                .oasysAssessmentSummary(OasysAssessmentSummary
                                        .builder()
                                        .assessedBy("?")
                                        .caseReferenceNumber("?")
                                        .concernFlags("?")
                                        .court("?")
                                        .courtType("?")
                                        .dateAssessmentCompleted("1970-01-01")
                                        .dateCreated("1970-01-01")
                                        .eventNumber("?")
                                        .layer1Obj("?")
                                        .layerType("?")
                                        .oasysId("?")
                                        .oasysSection2Scores("1")
                                        .oasysSection3Score("1")
                                        .oasysSection3Scores("1")
                                        .oasysSection4Score("1")
                                        .oasysSection4Scores("1")
                                        .oasysSection5Scores("1")
                                        .oasysSection6Score("1")
                                        .oasysSection6Scores("1")
                                        .oasysSection7Score("1")
                                        .oasysSection7Scores("1")
                                        .oasysSection8Score("1")
                                        .oasysSection8Scores("1")
                                        .oasysSection9Score("1")
                                        .oasysSection9Scores("1")
                                        .oasysSection10Scores("1")
                                        .oasysSection11Score("1")
                                        .oasysSection11Scores("1")
                                        .oasysSection12Score("1")
                                        .oasysSection12Scores("1")
                                        .oasysTotalScore("1")
                                        .offenceCode("?")
                                        .ogpNotCalculated("?")
                                        .ogpRiskRecon("?")
                                        .ogpScore1("1")
                                        .ogpScore2("1")
                                        .ogrsRiskRecon("?")
                                        .ogrsScore1("1")
                                        .ogrsScore2("1")
                                        .ovpNotCalculated("?")
                                        .ovpRiskRecon("?")
                                        .ovpScore1("1")
                                        .ovpScore2("1")
                                        .purposeOfAssessmentCode("?")
                                        .purposeOfAssessmentDescription("?")
                                        .reviewNumber("1")
                                        .reviewTerminated("?")
                                        .sentencePlanInitialDate("1970-01-01")
                                        .sentencePlanReviewDate("1970-01-01")
                                        .tierCode("?")
                                        .build())
                                .build())
                        .build())
                .build();

        final XmlMapper xmlMapper = new XmlMapper();
        String serialized = xmlMapper.writeValueAsString(builtMessage);

        Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSources(
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/common_types.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/assessment_summary.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/Risk.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/SubmitAssessmentSummary/submit_assessment_summary_request.xsd")).build(),
                Input.fromStream(ClassLoader.getSystemResourceAsStream("NDHtoDelius wsdls/soap.xsd")).build());

        ValidationResult result = v.validateInstance(Input.fromString(serialized).build());

        assertThat(result.isValid()).isTrue();

    }
}