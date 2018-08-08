package uk.gov.justice.digital.ndh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer;

@Service
public class OasysOffenderService {
    private final OffenderTransformer offenderTransformer;

    @Autowired
    public OasysOffenderService(OffenderTransformer offenderTransformer) {
        this.offenderTransformer = offenderTransformer;
    }
}
