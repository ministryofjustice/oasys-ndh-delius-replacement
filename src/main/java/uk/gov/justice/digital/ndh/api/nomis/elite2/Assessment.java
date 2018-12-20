package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@Value
public class Assessment {
    private Long bookingId;
    private String offenderNo;
    private String classification;
    private String assessmentCode;
    private String assessmentDescription;
    private boolean cellSharingAlertFlag;
    private LocalDate assessmentDate;
    private LocalDate nextReviewDate;
}
