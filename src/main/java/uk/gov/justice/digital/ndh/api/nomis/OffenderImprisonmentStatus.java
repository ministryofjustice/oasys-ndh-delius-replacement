package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class OffenderImprisonmentStatus {
    private Long bookingId;
    private Long imprisonStatusSeq;
    private ImprisonmentStatus imprisonmentStatus;
    private String imprisonmentStatusCode;
    private LocalDateTime effectiveDateTime;
    private LocalDate expiryDate;
    private String agencyLocationId;
    private String commentText;
    private Boolean latestStatus;
}
