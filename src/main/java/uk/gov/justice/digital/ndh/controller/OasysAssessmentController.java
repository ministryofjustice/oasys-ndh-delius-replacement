package uk.gov.justice.digital.ndh.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.api.oasys.CmsUpdate;

@RestController
public class OasysAssessmentController {

    @RequestMapping(path = "/oasysAssessments", method = RequestMethod.POST, consumes = {"application/xml", "text/xml", "text/plain"})
    public ResponseEntity<Void> handleOasysAssessment(@RequestBody CmsUpdate oasysAssessment) {

        oasysAssessment.toString();

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
