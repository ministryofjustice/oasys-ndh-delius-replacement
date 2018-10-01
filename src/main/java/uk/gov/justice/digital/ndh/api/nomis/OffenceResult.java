package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OffenceResult {
    private String Code;
    private String Description;

    private String DispositionCode;
    private Boolean ChargeStatus;
    private Boolean Conviction;
    private Boolean Active;
    private LocalDateTime ExpiryDate;
}
