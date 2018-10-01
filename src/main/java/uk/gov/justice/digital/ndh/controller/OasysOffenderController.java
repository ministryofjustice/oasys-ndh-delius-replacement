package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.service.OasysOffenderService;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;

import java.util.Optional;

@RestController
@Slf4j
public class OasysOffenderController {

    private final OasysOffenderService oasysOffenderService;
    private final CommonTransformer commonTransformer;

    @Autowired
    public OasysOffenderController(OasysOffenderService oasysOffenderService, CommonTransformer commonTransformer) {
        this.oasysOffenderService = oasysOffenderService;
        this.commonTransformer = commonTransformer;
    }

    @RequestMapping(path = "/${oasys.initial.search.path:initialSearch}", method = RequestMethod.POST, consumes = {"application/soap+xml", "application/xml", "text/xml", "text/plain"}, produces = "application/xml")
    public ResponseEntity<String> handleInitialSearch(@RequestBody String initialSearchXml) {
        log.info("Received POSTed initial search request beginning {}...", commonTransformer.limitLength(initialSearchXml, 50));
        final Optional<String> maybeResponse = oasysOffenderService.initialSearch(initialSearchXml);

        return maybeResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @RequestMapping(path = "/${oasys.offender.details.path:offenderDetails}", method = RequestMethod.POST, consumes = {"application/soap+xml", "application/xml", "text/xml", "text/plain"}, produces = "application/xml")
    public ResponseEntity<String> handleOffenderDetails(@RequestBody String offenderDetailsRequestXml) throws JsonProcessingException {
        log.info("Received POSTed offender details request beginning {}...", commonTransformer.limitLength(offenderDetailsRequestXml, 50));
        final Optional<String> maybeResponse = oasysOffenderService.offenderDetails(offenderDetailsRequestXml);

        return maybeResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
