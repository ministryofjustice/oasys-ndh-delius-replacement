package uk.gov.justice.digital.ndh.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.nomis.AgencyLocation;
import uk.gov.justice.digital.ndh.api.nomis.CourtEvent;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class OasysOffenderServiceTest {

    @Test
    public void sentencingAgencyLocationOfUsesCorrectEvent() {
        final Optional<AgencyLocation> actual = OasysOffenderService.sentencingAgencyLocationOf(Optional.of(ImmutableList.of(
                CourtEvent.builder().courtEventType("Colin").build(),
                CourtEvent.builder()
                        .courtEventType("SENT")
                        .agencyLocation(AgencyLocation.builder().agencyLocationId("HUDRCT").build())
                        .build()
        )));
        assertThat(actual).isPresent();
        assertThat(actual.get().getAgencyLocationId()).isEqualTo("HUDRCT");
    }
}