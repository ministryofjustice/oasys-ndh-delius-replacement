package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class Order {
    private Long orderId;
    private Long bookingId;
    private LocalDate courtDate;
    private String orderType;
    private String issuingAgencyLocationId;
    private String orderStatus;
    private LocalDate dueDate;
    private String courtSeriousnessLevel;
    private String orderSeriousnessLevel;
    private LocalDate requestDate;
    private Integer courtEventId;
    private LocalDate completeDate;
    private String interventionTierCode;
    private LocalDate issueDate;
    private String comments;

}
