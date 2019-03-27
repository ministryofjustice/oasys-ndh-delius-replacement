package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.rholder.retry.RetryException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;
import uk.gov.justice.digital.ndh.ThatsNotMyNDH;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.api.oasys.xtag.EventMessage;
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
        EventsPullerService eventsPullerService = new EventsPullerService(null, null, null, xtagTransformer, null, null, null, messageStoreRepository, 0);

        when(xtagTransformer.offenderImprisonmentStatusUpdatedXtagOf(any(OffenderEvent.class))).thenThrow(new NomisAPIServiceError("bang."));
        when(messageStoreRepository.findFirstByProcessNameOrderByMsgStoreSeqDesc(any(String.class))).thenReturn(Optional.of(MsgStore.builder().msgTimestamp(Timestamp.from(Instant.now())).build()));

        try {
            eventsPullerService.xtagEventMessageOf(OffenderEvent.builder().nomisEventType("OFF_IMP_STAT_OASYS").build());
        } catch (Throwable t) {
            fail("Shouldn't have thrown.");
        }
    }

    @Test
    public void eventsSentToOasysAreLoggedWithTheRawEventTimestamp() throws ExecutionException, RetryException, NomisAPIServiceError, OasysAPIServiceError, UnirestException, JsonProcessingException {
        final XtagTransformer xtagTransformer = mock(XtagTransformer.class);
        final MessageStoreRepository messageStoreRepository = mock(MessageStoreRepository.class);
        final MessageStoreService messageStoreService = mock(MessageStoreService.class);
        final OasysSOAPClient oasysSOAPClient = mock(OasysSOAPClient.class);

        final ThatsNotMyNDH thatsNotMyNDH = new ThatsNotMyNDH();
        XmlMapper xmlMapper = thatsNotMyNDH.xmlMapper(thatsNotMyNDH.xmlConverter());
        EventsPullerService eventsPullerService = new EventsPullerService(null, null, xmlMapper, null, oasysSOAPClient, null, messageStoreService, messageStoreRepository, 0);

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
}