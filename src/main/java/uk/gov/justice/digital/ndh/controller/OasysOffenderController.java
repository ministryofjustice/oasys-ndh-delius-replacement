package uk.gov.justice.digital.ndh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.ndh.service.OasysOffenderService;

import java.util.Optional;

@RestController
public class OasysOffenderController {

    private final OasysOffenderService oasysOffenderService;

    @Autowired
    public OasysOffenderController(OasysOffenderService oasysOffenderService) {
        this.oasysOffenderService = oasysOffenderService;
    }

    @RequestMapping(path = "/${oasys.initial.search.path:initialSearch}", method = RequestMethod.POST, consumes = {"application/xml", "text/xml", "text/plain"}, produces = "application/xml")
    public ResponseEntity<String> handleInitialSearch(@RequestBody String initialSearchXml) {

        final Optional<String> maybeResponse = oasysOffenderService.initialSearch(initialSearchXml);

        return maybeResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
