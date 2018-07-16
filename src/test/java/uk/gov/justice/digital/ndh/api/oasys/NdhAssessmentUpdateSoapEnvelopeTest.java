package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class NdhAssessmentUpdateSoapEnvelopeTest {

    @Test
    public void canDeserialiseInputWithManyObjectives() throws IOException {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final NdhAssessmentUpdateSoapEnvelope ndhAssessmentUpdateSoapEnvelope =
                xmlMapper.readValue(ClassLoader.getSystemResourceAsStream("xmls/GeneratedSampleFromOasys.xml"), NdhAssessmentUpdateSoapEnvelope.class);

        final List<Objective> objectives = ndhAssessmentUpdateSoapEnvelope.getBody().getCmsUpdate().getObjectives();
        assertThat(objectives).extracting("objectiveDescription").containsExactly("objective1", "objective2");
    }
}