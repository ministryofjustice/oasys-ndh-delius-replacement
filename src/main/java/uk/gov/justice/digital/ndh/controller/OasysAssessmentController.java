package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.api.delius.OasysCommonHeader;
import uk.gov.justice.digital.ndh.api.delius.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.SoapBody;
import uk.gov.justice.digital.ndh.api.delius.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.SoapHeader;
import uk.gov.justice.digital.ndh.api.delius.SubmitAssessmentSummaryRequest;
import uk.gov.justice.digital.ndh.api.oasys.CmsUpdate;

@RestController
@Slf4j
public class OasysAssessmentController {

    @Autowired
    XmlMapper xmlMapper;

    @RequestMapping(path = "/oasysAssessments", method = RequestMethod.POST, consumes = {"application/xml", "text/xml", "text/plain"})
    public ResponseEntity<Void> handleOasysAssessment(@RequestBody CmsUpdate oasysAssessment) {

        oasysAssessment.toString();

        final SoapEnvelope thing = SoapEnvelope.builder()
                .header(SoapHeader
                        .builder()
                        .commonHeader(OasysCommonHeader
                                .builder()
                                .messageId("x")
                                .version("x")
                                .build())
                        .build())
                .body(SoapBody
                        .builder()
                        .request(SubmitAssessmentSummaryRequest
                                .builder()
                                .oasysSupervisionPlan(OasysSupervisionPlan
                                        .builder()
                                        .oasysId("x")
                                        .caseReferenceNumber("x")
                                        .need1("x")
                                        .need2("x")
                                        .need3("x")
                                        .need4("x")
                                        .objective("x")
                                        .objectiveNumber("x")
                                        .objectiveStatus("x")
                                        .text1("x")
                                        .text2("x")
                                        .text3("x")
                                        .workSummary1("x")
                                        .workSummary2("x")
                                        .workSummary3("x")
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            String s = xmlMapper.writeValueAsString(thing);
            log.info(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
