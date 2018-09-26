package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Physicals {
    private Long bookingId;
    private List<IdentifyingMark> identifyingMarks;
    private List<PhysicalAttribute> physicalAttributes;
    private List<ProfileDetails> profileDetails;
}
