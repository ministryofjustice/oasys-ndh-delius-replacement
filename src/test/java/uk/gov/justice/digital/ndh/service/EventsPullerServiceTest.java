package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.ImmutableList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;
import uk.gov.justice.digital.ndh.ThatsNotMyNDH;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_1;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;
import uk.gov.justice.digital.ndh.service.exception.OasysAPIServiceError;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventsPullerServiceTest {

    @Test
    public void nomisService404sAreIgnored() throws ExecutionException, RetryException, NomisAPIServiceError {
        final XtagTransformer xtagTransformer = mock(XtagTransformer.class);
        final MessageStoreRepository messageStoreRepository = mock(MessageStoreRepository.class);
        EventsPullerService eventsPullerService = new EventsPullerService(null, xtagTransformer, null, null, null, messageStoreRepository, 0, null);

        when(xtagTransformer.offenderImprisonmentStatusUpdatedXtagOf(any(OffenderEvent.class))).thenThrow(new NomisAPIServiceError("bang."));
        when(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc(any(String.class))).thenReturn(Optional.of(MsgStore.builder().msgTimestamp(Timestamp.from(Instant.now())).build()));

        try {
            eventsPullerService.xtagEventMessageOf(OffenderEvent.builder().nomisEventType("OFF_IMP_STAT_OASYS").build());
        } catch (Throwable t) {
            fail("Shouldn't have thrown.");
        }
    }

    @Test
    public void eventsSentToOasysAreLoggedWithTheRawEventTimestamp() throws OasysAPIServiceError, UnirestException, JsonProcessingException {
        final MessageStoreRepository messageStoreRepository = mock(MessageStoreRepository.class);
        final MessageStoreService messageStoreService = mock(MessageStoreService.class);
        final OasysSOAPClient oasysSOAPClient = mock(OasysSOAPClient.class);

        final ThatsNotMyNDH thatsNotMyNDH = new ThatsNotMyNDH();
        XmlMapper xmlMapper = thatsNotMyNDH.xmlMapper(thatsNotMyNDH.xmlConverter());
        EventsPullerService eventsPullerService = new EventsPullerService(xmlMapper, null, oasysSOAPClient, null, messageStoreService, messageStoreRepository, 0, null);

        when(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc(any(String.class))).thenReturn(Optional.of(MsgStore.builder().msgTimestamp(Timestamp.from(Instant.now())).build()));
        final HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        when(oasysSOAPClient.oasysWebServiceResponseOf(any(String.class))).thenReturn(httpResponse);

        final LocalDateTime now = LocalDateTime.now();
        doAnswer(invocation -> {
            assertThat(invocation.<LocalDateTime>getArgument(5)).isEqualTo(now);
            return null;
        }).when(messageStoreService).writeMessage(any(String.class), any(String.class), any(String.class), any(String.class), any(MessageStoreService.ProcStates.class), any(LocalDateTime.class));

        eventsPullerService.sendToOAsys(Optional.of(EventMessage.builder()
                .rawEventDateTime(now)
                .build()));

        verify(messageStoreService).writeMessage(anyString(), eq(null), eq(null), eq("XTAG"), eq(MessageStoreService.ProcStates.GLB_ProcState_OutboundAfterTransformation), eq(now));

    }

    @Test
    public void poisonousMessageAdvancesPulledFromToTheLastSuccessfull() throws ExecutionException, RetryException, NomisAPIServiceError, UnirestException, JsonProcessingException {
        var now = LocalDateTime.now();
        final XtagTransformer xtagTransformer = mock(XtagTransformer.class);
        final MessageStoreRepository messageStoreRepository = mock(MessageStoreRepository.class);
        final NomisApiServices nomisApiServices = mock(NomisApiServices.class);
        final MessageStoreService messageStoreService = mock(MessageStoreService.class);
        final OasysSOAPClient oasysSOAPClient = mock(OasysSOAPClient.class);
        final XmlMapper xmlMapper = mock(XmlMapper.class);

        OffenderEvent event1 = OffenderEvent.builder().nomisEventType("OFF_RECEP_OASYS").eventDatetime(now.minusHours(3L)).build();
        OffenderEvent event2 = OffenderEvent.builder().nomisEventType("OFF_RECEP_OASYS").eventDatetime(now.minusHours(2L)).build();
        OffenderEvent event3 = OffenderEvent.builder().nomisEventType("OFF_RECEP_OASYS").eventDatetime(now.minusHours(1L)).build();

        var events = ImmutableList.of(event1, event2, event3);

        when(nomisApiServices.getEvents(any(LocalDateTime.class), any(LocalDateTime.class), eq(xtagTransformer))).thenReturn(Optional.of(events));
        when(xtagTransformer.offenderReceptionXtagOf(eq(event1))).thenReturn(Optional.of(EventMessage.builder().build()));
        when(xtagTransformer.offenderReceptionXtagOf(eq(event2))).thenReturn(Optional.of(EventMessage.builder().build()));
        when(xtagTransformer.offenderReceptionXtagOf(eq(event3))).thenThrow(new NullPointerException("bang."));
        final HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        when(oasysSOAPClient.oasysWebServiceResponseOf(any(String.class))).thenReturn(httpResponse);

        when(xmlMapper.writeValueAsString(any(Object.class))).thenReturn("");

        EventsPullerService eventsPullerService = new EventsPullerService(xmlMapper, xtagTransformer, oasysSOAPClient, null, messageStoreService, messageStoreRepository, 0, nomisApiServices);

        eventsPullerService.pullEvents();

        assertThat(eventsPullerService.getLastPulled()).isEqualTo(event2.getEventDatetime());
    }

    @Test
    public void soapIsSentToOAsys() throws OasysAPIServiceError, UnirestException, JsonProcessingException {
        final MessageStoreRepository messageStoreRepository = mock(MessageStoreRepository.class);
        final MessageStoreService messageStoreService = mock(MessageStoreService.class);
        final OasysSOAPClient oasysSOAPClient = mock(OasysSOAPClient.class);

        final ThatsNotMyNDH thatsNotMyNDH = new ThatsNotMyNDH();
        XmlMapper xmlMapper = thatsNotMyNDH.xmlMapper(thatsNotMyNDH.xmlConverter());
        EventsPullerService eventsPullerService = new EventsPullerService(xmlMapper, null, oasysSOAPClient, null, messageStoreService, messageStoreRepository, 0, null);

        when(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc(any(String.class))).thenReturn(Optional.of(MsgStore.builder().msgTimestamp(Timestamp.from(Instant.now())).build()));
        final HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        when(oasysSOAPClient.oasysWebServiceResponseOf(any(String.class))).thenReturn(httpResponse);

        final LocalDateTime now = LocalDateTime.now();
        doAnswer(invocation -> {
            assertThat(invocation.<LocalDateTime>getArgument(5)).isEqualTo(now);
            return null;
        }).when(messageStoreService).writeMessage(any(String.class), any(String.class), any(String.class), any(String.class), any(MessageStoreService.ProcStates.class), any(LocalDateTime.class));

        final EventMessage eventMessage = EventMessage.builder()
                .rawEventDateTime(now)
                .build();

        var expected = xmlMapper.writeValueAsString(SoapEnvelopeSpec1_1.builder().body(SoapBody.builder().eventMessage(eventMessage).build()).build());
        eventsPullerService.sendToOAsys(Optional.of(eventMessage));


        verify(oasysSOAPClient).oasysWebServiceResponseOf(expected);
    }
}