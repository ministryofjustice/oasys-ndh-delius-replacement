package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OutcomeReason {
    private String Code;
    private String Description;

    private String DispositionCode;
    private Boolean ChargeStatus;
    private Boolean Conviction;
    private Boolean Active;
    private LocalDateTime ExpiryDate;

}
