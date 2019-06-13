package uk.gov.justice.digital.ndh.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.nomis.AgencyLocation;
import uk.gov.justice.digital.ndh.api.nomis.Charge;
import uk.gov.justice.digital.ndh.api.nomis.CourtEvent;
import uk.gov.justice.digital.ndh.api.nomis.Sentence;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class OasysOffenderServiceTest {

    @Test
    public void sentencingAgencyLocationOfUsesCourtEventLocationForSentence() {
        final Optional<AgencyLocation> actual = OasysOffenderService.sentencingAgencyLocationOf(Optional.of(ImmutableList.of(
                CourtEvent.builder().courtEventType("Colin").build(),
                CourtEvent.builder()
                        .courtEventType("SENT")
                        .agencyLocation(AgencyLocation.builder().agencyLocationId("CROMER").build())
                        .build()
        ,
                CourtEvent.builder().courtEventType("Colin").build(),
                CourtEvent.builder()
                        .courtEventType("CHAINSAWS")
                        .agencyLocation(AgencyLocation.builder().agencyLocationId("HUDRCT").build())
                        .courtEventCharges(ImmutableList.of(Charge.builder().sentences(ImmutableList.of(Sentence.builder().sentenceSequenceNumber(1234).build())).build()))
                        .build()
        )), Optional.of(Sentence.builder().sentenceSequenceNumber(1234).build()));

        assertThat(actual).isPresent();
        assertThat(actual.get().getAgencyLocationId()).isEqualTo("HUDRCT");
    }

    @Test
    public void sentencingAgencyLocationOfHandlesCourtEventChargesWithNoSentence() {
        final Optional<AgencyLocation> actual = OasysOffenderService.sentencingAgencyLocationOf(Optional.of(ImmutableList.of(
                CourtEvent.builder().courtEventType("Colin").build(),
                CourtEvent.builder()
                        .courtEventType("SENT")
                        .agencyLocation(AgencyLocation.builder().agencyLocationId("CROMER").build())
                        .build()
                ,
                CourtEvent.builder().courtEventType("Colin").build(),
                CourtEvent.builder()
                        .courtEventType("CHAINSAWS")
                        .agencyLocation(AgencyLocation.builder().agencyLocationId("HUDRCT").build())
                        .courtEventCharges(ImmutableList.of(Charge.builder().build()))
                        .build()
        )), Optional.of(Sentence.builder().sentenceSequenceNumber(1234).build()));

        assertThat(actual).isEmpty();
    }
}