package uk.gov.justice.digital.ndh.api.delius.request;

import lombok.Value;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;

@Value
public class InitialSearchRequest {
    private Header header;
    private String cmsProbNumber;
    private String familyName;
    private String forename1;
}
