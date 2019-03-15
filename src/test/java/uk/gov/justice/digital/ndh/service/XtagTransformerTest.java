package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.ThatsNotMyNDH;
import uk.gov.justice.digital.ndh.api.nomis.Booking;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderAlias;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.nomis.elite2.InmateDetail;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class XtagTransformerTest {

    @Test
    public void pncIsNormalised() {
        assertThat(XtagTransformer.normalisedPncOf("1978/0111942M")).isEqualTo("78/0111942M");
        assertThat(XtagTransformer.normalisedPncOf("78/0111942M")).isEqualTo("78/0111942M");
    }

    @Test
    public void pncOfOffenderIsDerivedFromAllIdentifiers() {
        Offender anOffenderWithIdentifiersAtRootLevel = Offender.builder()
                .identifiers(ImmutableList.of(Identifier.builder()
                        .identifierType("PNC")
                        .identifier("70/1122A")
                        .build()))
                .build();

        Offender anOffenderWithoutIdentifiersAtRootLevel = Offender.builder()
                .aliases(ImmutableList.of(OffenderAlias.builder()
                        .identifiers(ImmutableList.of(Identifier.builder()
                                .identifierType("PNC")
                                .identifier("71/3344B")
                                .build()))
                        .build())
                ).build();

        Offender anOffenderWithIdentifiersAtRootLevelAndAliases = Offender.builder()
                .identifiers(ImmutableList.of(Identifier.builder()
                        .identifierType("PNC")
                        .identifier("72/5566C")
                        .build()))
                .aliases(ImmutableList.of(OffenderAlias.builder()
                        .identifiers(ImmutableList.of(Identifier.builder()
                                .identifierType("PNC")
                                .identifier("73/7788D")
                                .build()))
                        .build())
                ).build();

        assertThat(XtagTransformer.pncOf(anOffenderWithIdentifiersAtRootLevel)).isEqualTo("70/1122A");
        assertThat(XtagTransformer.pncOf(anOffenderWithoutIdentifiersAtRootLevel)).isEqualTo("71/3344B");
        assertThat(XtagTransformer.pncOf(anOffenderWithIdentifiersAtRootLevelAndAliases)).isEqualTo("72/5566C");
    }

    @Test
    public void receptionMovementCodeIsMappedIfPresentInReceptionCodeMapping() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, mappingService, null, null);

        when(mappingService.targetValueOf("C5", 2015L, false)).thenReturn("Jeremy");

        assertThat(xtagTransformer.receptionMovementCodeOf("C5")).isEqualTo("Jeremy");
    }

    @Test
    public void receptionMovementCodeIsMappedToRIfIfNotPresentInReceptionCodeMappingButIsPresentInDischargeMapping() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, mappingService, null, null);

        when(mappingService.targetValueOf("C5", 2015L, false)).thenThrow(NDHMappingException.builder().build());
        when(mappingService.targetValueOf("C5", 2016L, false)).thenReturn("Colin");

        assertThat(xtagTransformer.receptionMovementCodeOf("C5")).isEqualTo("R");
    }

    @Test(expected = NDHMappingException.class)
    public void mappingExceptionIsThrownIfReceptionMovementCodeNotMappedInEitherReceptionCodesOrDischargeCodes() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, mappingService, null, null);

        when(mappingService.targetValueOf("C5", 2015L, false)).thenThrow(NDHMappingException.builder().build());
        when(mappingService.targetValueOf("C5", 2016L)).thenThrow(NDHMappingException.builder().build());

        xtagTransformer.receptionMovementCodeOf("C5");
    }

    @Test
    public void nameDetailsAreTakenFromTheBookingOffender() throws IOException {
        final MappingService mappingService = mock(MappingService.class);
        final NomisApiServices nomisApiServices = mock(NomisApiServices.class);
        final ObjectMapper objectMapper = new ThatsNotMyNDH().objectMapper();
        final CorrelationService correlationService = mock(CorrelationService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(objectMapper, mappingService, nomisApiServices, correlationService);

        /*
        Error scenario:
        OffenderEvent(eventId=null, eventType=OFFENDER_BOOKING-CHANGED, eventDatetime=2019-03-05T15:20:42.473316, rootOffenderId=null, offenderId=1234567, aliasOffenderId=null, previousOffenderId=null, offenderIdDisplay=null, bookingId=2222222, bookingNumber=null, previousBookingNumber=null, sanctionSeq=null, movementSeq=null, imprisonmentStatusSeq=null, assessmentSeq=null, alertSeq=null, alertDateTime=null, alertType=null, alertCode=null, expiryDateTime=null, agencyLocationId=null, riskPredictorId=null, addressId=null, personId=null, sentenceCalculationId=null, oicHearingId=null, oicOffenceId=null, pleaFindingCode=null, findingCode=null, resultSeq=null, agencyIncidentId=null, chargeSeq=null, identifierType=null, identifierValue=null, ownerId=null, ownerClass=null, sentenceSeq=null, conditionCode=null, offenderSentenceConditionId=null, addressEndDate=null, primaryAddressFlag=null, mailAddressFlag=null, addressUsage=null, movementDateTime=null, movementType=null, movementReasonCode=null, directionCode=null, escortCode=null, fromAgencyLocationId=null, toAgencyLocationId=null, nomisEventType=OFF_UPD_OASYS)
         */
        OffenderEvent event = OffenderEvent.builder()
                .eventType("OFFENDER_BOOKING-CHANGED")
                .eventDatetime(LocalDateTime.parse("2019-03-05T15:20:42.473316"))
                .offenderId(Long.valueOf("1234567"))
                .bookingId(Long.valueOf("2222222"))
                .nomisEventType("OFF_UPD_OASYS")
                .build();

        InmateDetail inmateDetail = objectMapper.readValue("{\n" +
                "    \"bookingId\": 2222222,\n" +
                "    \"bookingNo\": \"33333B\",\n" +
                "    \"offenderNo\": \"A0007AA\",\n" +
                "    \"firstName\": \"JAN\",\n" +
                "    \"middleName\": \"MICHAEL\",\n" +
                "    \"lastName\": \"VINCENT\",\n" +
                "    \"agencyId\": \"DNI\",\n" +
                "    \"assignedLivingUnitId\": 292929,\n" +
                "    \"activeFlag\": true,\n" +
                "    \"dateOfBirth\": \"1984-01-16\"\n" +
                "}", InmateDetail.class);


        Offender rootOffender = objectMapper.readValue("{\n" +
                "  \"nomsId\": \"A0007AA\",\n" +
                "  \"offenderId\": 8888888,\n" +
                "  \"firstName\": \"JAN\",\n" +
                "  \"surname\": \"VINCENT\",\n" +
                "  \"dateOfBirth\": \"1984-01-16\",\n" +
                "  \"aliases\": [\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JAN\",\n" +
                "      \"middleNames\": \"MICHAEL\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1984-01-16\",\n" +
                "      \"offenderId\": 1234567,\n" +
                "      \"identifiers\": [\n" +
                "        {\n" +
                "          \"identifierType\": \"PNC\",\n" +
                "          \"identifier\": \"97/121212X\",\n" +
                "          \"sequenceNumber\": 2,\n" +
                "          \"createdDateTime\": \"2019-03-05T15:21:55.944254\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JAN\",\n" +
                "      \"middleNames\": \"MICHAEL\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1980-06-09\",\n" +
                "      \"offenderId\": 9988776\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JANET\",\n" +
                "      \"middleNames\": \"MICHAEL\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1984-01-16\",\n" +
                "      \"offenderId\": 5544332\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JAN\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1980-06-09\",\n" +
                "      \"offenderId\": 7777776\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JAN\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1984-11-16\",\n" +
                "      \"offenderId\": 6565656\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"JAN\",\n" +
                "      \"middleNames\": \"L\",\n" +
                "      \"surname\": \"VINCENT\",\n" +
                "      \"dateOfBirth\": \"1984-01-16\",\n" +
                "      \"offenderId\": 4545454\n" +
                "    },\n" +
                "    {\n" +
                "      \"nomsId\": \"A0007AA\",\n" +
                "      \"firstName\": \"BILLY\",\n" +
                "      \"surname\": \"VINNOCENT\",\n" +
                "      \"dateOfBirth\": \"1984-11-16\",\n" +
                "      \"offenderId\": 3434343\n" +
                "    }\n" +
                "  ],\n" +
                "  \"bookings\": [\n" +
                "    {\n" +
                "      \"bookingId\": 2222222,\n" +
                "      \"bookingNo\": \"33333B\",\n" +
                "      \"bookingSequence\": 1,\n" +
                "      \"offenderId\": 1234567,\n" +
                "      \"rootOffenderId\": 8888888,\n" +
                "      \"activeFlag\": true,\n" +
                "      \"agencyLocation\": {\n" +
                "          \"agencyLocationId\": \"OUT\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookingId\": 3333333,\n" +
                "      \"bookingNo\": \"83838A\",\n" +
                "      \"offenderId\": 8888888,\n" +
                "      \"rootOffenderId\": 8888888\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookingId\": 4444444,\n" +
                "      \"bookingNo\": \"R00001\",\n" +
                "      \"offenderId\": 8888888,\n" +
                "      \"rootOffenderId\": 8888888\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookingId\": 5555555,\n" +
                "      \"bookingNo\": \"N00001\",\n" +
                "      \"offenderId\": 8888888,\n" +
                "      \"rootOffenderId\": 8888888\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookingId\": 6666666,\n" +
                "      \"bookingNo\": \"LK7777\",\n" +
                "      \"offenderId\": 8888888,\n" +
                "      \"rootOffenderId\": 8888888\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookingId\": 7777777,\n" +
                "      \"bookingNo\": \"LK7777\",\n" +
                "      \"offenderId\": 8888888,\n" +
                "      \"rootOffenderId\": 8888888\n" +
                "    }\n" +
                "  ],\n" +
                "  \"identifiers\": [\n" +
                "    {\n" +
                "      \"identifierType\": \"MERGE_HMPS\",\n" +
                "      \"identifier\": \"LK7777\",\n" +
                "      \"sequenceNumber\": 2,\n" +
                "      \"createdDateTime\": \"2012-02-28T16:29:13.731715\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"identifierType\": \"MERGED\",\n" +
                "      \"identifier\": \"A6666CL\",\n" +
                "      \"sequenceNumber\": 1,\n" +
                "      \"createdDateTime\": \"2012-02-28T16:29:13.727351\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", Offender.class);

        Offender thisOffender = objectMapper.readValue("  {\n" +
                "    \"nomsId\": \"A0007AA\",\n" +
                "    \"offenderId\": 1234567,\n" +
                "    \"firstName\": \"JAN\",\n" +
                "    \"middleNames\": \"MICHAEL\",\n" +
                "    \"surname\": \"VINCENT\",\n" +
                "    \"dateOfBirth\": \"1984-01-16\"\n" +
                "  }", Offender.class);

        when(correlationService.nextCorrelationId()).thenReturn("a_correlation");
        when(mappingService.targetValueOf("OUT", 2005L)).thenReturn("MEH");
        try {
            when(nomisApiServices.getOffenderByNomsId("A0007AA")).thenReturn(rootOffender);
            when(nomisApiServices.getOffenderByOffenderId(1234567L)).thenReturn(thisOffender);
            when(nomisApiServices.getInmateDetail(2222222L, xtagTransformer)).thenReturn(inmateDetail);

            final Optional<EventMessage> maybeEventMessage = xtagTransformer.offenderUpdatedXtagOf(event);

            assertThat(maybeEventMessage.isPresent()).isTrue();

            assertThat(maybeEventMessage.get().getForename2()).isEqualTo("MICHAEL");

        } catch (ExecutionException | NomisAPIServiceError | RetryException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void bookingNoBehavesAppropriately() {
        final MappingService mappingService = mock(MappingService.class);
        final NomisApiServices nomisApiServices = mock(NomisApiServices.class);
        final ObjectMapper objectMapper = new ThatsNotMyNDH().objectMapper();
        final CorrelationService correlationService = mock(CorrelationService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(objectMapper, mappingService, nomisApiServices, correlationService);

        assertThat(xtagTransformer.bookingNoOf(Offender.builder().build())).isNull();
        assertThat(xtagTransformer.bookingNoOf(Offender.builder()
                .bookings(ImmutableList.of(Booking.builder().bookingNo("MEMEME").build()))
                .build())).isEqualTo("MEMEME");
    }

}