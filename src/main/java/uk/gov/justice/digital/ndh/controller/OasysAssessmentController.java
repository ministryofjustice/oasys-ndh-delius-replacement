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
import uk.gov.justice.digital.ndh.api.delius.OasysAssessmentSummary;
import uk.gov.justice.digital.ndh.api.delius.OasysCommonHeader;
import uk.gov.justice.digital.ndh.api.delius.OasysSupervisionPlan;
import uk.gov.justice.digital.ndh.api.delius.RiskType;
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
        return new ResponseEntity<>(HttpStatus.OK);

    }

}
