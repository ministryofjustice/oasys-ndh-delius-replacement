package uk.gov.justice.digital.ndh.service.transforms;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.api.delius.request.Header;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

@Component
public class CommonTransformer {

    public static final String VERSION = "1.0";

    public Header headerOf(String correlationId) {
        return Header
                .builder()
                .messageId(correlationId)
                .version(VERSION)
                .build();
    }

    public SoapHeader deliusSoapHeaderOf(String correlationID) {
        return SoapHeader
                .builder()
                .header(headerOf(correlationID))
                .build();
    }
}
