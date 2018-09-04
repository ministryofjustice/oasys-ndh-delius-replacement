package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;

import javax.jms.Queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OasysAssessmentServiceTest {

    private static final XmlMapper xmlMapper = new XmlMapper();

    @Test
    public void canPostMessageToJmsQueue() throws JsonProcessingException {
        final JmsTemplate mockTemplate = mock(JmsTemplate.class);
        final Queue mockQueue = mock(Queue.class);
        final DeliusSOAPClient mockDeliusAssessmentUpdateClient = mock(DeliusSOAPClient.class);

        OasysAssessmentService service = new OasysAssessmentService(mockTemplate, mockQueue, mockDeliusAssessmentUpdateClient);
        CmsUpdate update = CmsUpdate.builder().build();

        final String updateXml = xmlMapper.writeValueAsString(update);
        service.publishAssessmentUpdate(updateXml);

        verify(mockTemplate).convertAndSend(mockQueue, updateXml);
    }
}