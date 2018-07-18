package uk.gov.justice.digital.ndh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.justice.digital.ndh.api.oasys.request.CmsUpdate;

import javax.jms.Queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OasysAssessmentServiceTest {

    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void canPostMessageToJmsQueue() throws JsonProcessingException {
        final JmsTemplate mockTemplate = mock(JmsTemplate.class);
        final Queue mockQueue = mock(Queue.class);

        OasysAssessmentService service = new OasysAssessmentService(mockTemplate, mockQueue, "abc", xmlMapper, objectMapper);
        CmsUpdate update = CmsUpdate.builder().build();

        final String updateXml = xmlMapper.writeValueAsString(update);
        service.publishUpdate(updateXml);

        verify(mockTemplate).convertAndSend(mockQueue, updateXml);
    }
}