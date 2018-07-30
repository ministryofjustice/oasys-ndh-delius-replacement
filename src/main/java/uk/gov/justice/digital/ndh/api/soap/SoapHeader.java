package uk.gov.justice.digital.ndh.api.soap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SoapHeader {
    public String correlationId;
}
