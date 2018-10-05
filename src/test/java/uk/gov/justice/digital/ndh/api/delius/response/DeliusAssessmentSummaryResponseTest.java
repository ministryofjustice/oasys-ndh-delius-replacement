package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliusAssessmentSummaryResponseTest {

    @Test
    public void canCorrectlyIdentifyASoapFault() throws IOException {
        String faulty = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<SOAP-ENV:Header/><SOAP-ENV:Body>\n" +
                "<SOAP-ENV:Fault><faultcode>SOAP-ENV:Client</faultcode>\n" +
                "<faultstring>Message does not have necessary info</faultstring>\n" +
                "<faultactor>http://gizmos.com/order</faultactor>\n" +
                "<detail>\n" +
                "<PO:order xmlns:PO=\"http://gizmos.com/orders/\">\n" +
                "Quantity element does not have a value</PO:order>\n" +
                "<PO:confirmation xmlns:PO=\"http://gizmos.com/confirm\">\n" +
                "Incomplete address: no zip code</PO:confirmation>\n" +
                "</detail></SOAP-ENV:Fault>\n" +
                "</SOAP-ENV:Body></SOAP-ENV:Envelope>\n";

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final SoapEnvelopeSpec1_2 deliusResponse = xmlMapper.readValue(faulty, SoapEnvelopeSpec1_2.class);

        final JsonNode fault = deliusResponse.getBody().getFault();

        assertThat(deliusResponse.getBody().isSoapFault()).isTrue();
        assertThat(fault.get("faultstring").textValue()).isEqualTo("Message does not have necessary info");
    }

    @Test
    public void canCorrectlyIdentifyANonSoapFault() throws IOException {
        String notFaulty = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<SOAP-ENV:Header/><SOAP-ENV:Body>\n" +
                "<IsItFine>yes</IsItFine>\n" +
                "</SOAP-ENV:Body></SOAP-ENV:Envelope>\n";

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final SoapEnvelopeSpec1_2 deliusResponse = xmlMapper.readValue(notFaulty, SoapEnvelopeSpec1_2.class);

        assertThat(deliusResponse.getBody().isSoapFault()).isFalse();
    }
}