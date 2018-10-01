package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class Offender {
    private String nomsId;
    private Long offenderId;
    private String firstName;
    private String middleNames;
    private String surname;
    private LocalDate dateOfBirth;
    private KeyValue gender;
    private KeyValue ethnicity;
    private List<OffenderAlias> aliases;
    private List<Booking> bookings;
    private List<Identifier> identifiers;

}
