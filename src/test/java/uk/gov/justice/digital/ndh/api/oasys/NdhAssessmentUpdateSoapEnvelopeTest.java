package uk.gov.justice.digital.ndh.api.oasys;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.oasys.request.Objective;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class NdhAssessmentUpdateSoapEnvelopeTest {

    @Test
    public void canDeserialiseInputWithManyObjectives() throws IOException {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final SoapEnvelopeSpec1_2 ndhAssessmentUpdateSoapEnvelope =
                xmlMapper.readValue(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/GeneratedSampleFromOasys.xml"), SoapEnvelopeSpec1_2.class);

        final List<Objective> objectives = ndhAssessmentUpdateSoapEnvelope.getBody().getCmsUpdate().getObjectives();
        assertThat(objectives).extracting("objectiveDescription").containsExactly("objective1", "objective2");
    }
}