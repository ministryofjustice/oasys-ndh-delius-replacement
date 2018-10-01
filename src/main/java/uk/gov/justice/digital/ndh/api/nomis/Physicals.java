package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Physicals {
    private Long bookingId;
    private List<IdentifyingMark> identifyingMarks;
    private List<PhysicalAttribute> physicalAttributes;
    private List<ProfileDetails> profileDetails;
}
