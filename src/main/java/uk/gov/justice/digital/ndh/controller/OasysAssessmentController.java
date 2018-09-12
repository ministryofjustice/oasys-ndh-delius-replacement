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
import uk.gov.justice.digital.ndh.service.OasysRiskService;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;

import java.util.Optional;

@RestController
@Slf4j
public class OasysAssessmentController {

    private final OasysAssessmentService oasysAssessmentService;
    private final OasysRiskService oasysRiskService;
    private final CommonTransformer commonTransformer;

    @Autowired
    public OasysAssessmentController(OasysAssessmentService oasysAssessmentService, OasysRiskService oasysRiskService, CommonTransformer commonTransformer) {
        this.oasysAssessmentService = oasysAssessmentService;
        this.oasysRiskService = oasysRiskService;
        this.commonTransformer = commonTransformer;
    }

    @RequestMapping(path = "/${oasys.assessment.updates.path:oasysAssessments}", method = RequestMethod.POST, consumes = {"application/soap+xml", "application/xml", "text/xml", "text/plain"})
    public ResponseEntity<Void> handleOasysAssessment(@RequestBody String updateXml) {
        log.info("Received POSTed assessment update request beginning {}...", commonTransformer.limitLength(updateXml, 30));

        oasysAssessmentService.publishAssessmentUpdate(updateXml);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(path = "/${oasys.risk.updates.path:oasysRiskUpdates}", method = RequestMethod.POST, consumes = {"application/soap+xml", "application/xml", "text/xml", "text/plain"}, produces = "application/xml")
    public ResponseEntity<String> handleOasysRiskUpdate(@RequestBody String updateXml) {

        log.info("Received POSTed risk update request beginning {}...", commonTransformer.limitLength(updateXml, 30));

        final Optional<String> maybeResponse = oasysRiskService.processRiskUpdate(updateXml);

        return maybeResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
