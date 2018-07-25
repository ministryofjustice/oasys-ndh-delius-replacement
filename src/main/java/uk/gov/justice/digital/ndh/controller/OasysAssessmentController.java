package uk.gov.justice.digital.ndh.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.service.OasysAssessmentService;

@RestController
@Slf4j
public class OasysAssessmentController {

    private final OasysAssessmentService oasysAssessmentService;

    @Autowired
    public OasysAssessmentController(OasysAssessmentService oasysAssessmentService) {
        this.oasysAssessmentService = oasysAssessmentService;
    }

    @RequestMapping(path = "/${oasys.assessment.updates.path:oasysAssessments}", method = RequestMethod.POST, consumes = {"application/xml", "text/xml", "text/plain"})
    public ResponseEntity<Void> handleOasysAssessment(@RequestBody String updateXml) {

        oasysAssessmentService.publishUpdate(updateXml);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
