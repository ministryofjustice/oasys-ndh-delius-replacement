package uk.gov.justice.digital.ndh.service;

import com.github.rholder.retry.RetryException;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.nomis.OffenderEvent;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;
import uk.gov.justice.digital.ndh.service.exception.NomisAPIServiceError;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
}