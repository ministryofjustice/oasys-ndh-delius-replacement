package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapBody;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapHeader;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.Risk;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponseSoapBody;
import uk.gov.justice.digital.ndh.api.oasys.response.SubmitRiskDataResponseSoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OasysRiskUpdateTransformerTest {

    public static final String NOW = LocalDateTime.now().toString();

    @Test
    public void oasysRiskUpdateIsTransformedCorrectly() {

        final SoapEnvelope oasysRequest = anOasysRiskUpdate();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer();

        DeliusRiskUpdateSoapEnvelope expected = DeliusRiskUpdateSoapEnvelope
                .builder()
                .header(DeliusRiskUpdateSoapHeader
                        .builder()
                        .header(uk.gov.justice.digital.ndh.api.delius.request.Header
                                .builder()
                                .messageId("corrId")
                                .version("1.0")
                                .build())
                        .build())
                .body(DeliusRiskUpdateSoapBody
                        .builder()
                        .submitRiskDataRequest(uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest
                                .builder()
                                .risk(uk.gov.justice.digital.ndh.api.delius.request.Risk
                                        .builder()
                                        .caseReferenceNumber("A1234")
                                        .riskOfHarm("riskOfHarm")
                                        .build())
                                .build())
                        .build())
                .build();

        DeliusRiskUpdateSoapEnvelope actual = transformer.deliusRiskUpdateRequestOf(oasysRequest);

        assertThat(actual).isEqualTo(expected);


    }

    private SoapEnvelope anOasysRiskUpdate() {
        return SoapEnvelope
                .builder()
                .header(SoapHeader.builder().build())
                .body(SoapBody
                        .builder()
                        .riskUpdateRequest(SubmitRiskDataRequest
                                .builder()
                                .cmsProbNumber("A1234")
                                .header(Header
                                        .builder()
                                        .applicationMode("appMode")
                                        .correlationID("corrId")
                                        .messageTimestamp(NOW)
                                        .oasysRUsername("oasysRUsername")
                                        .build())
                                .risk(Risk
                                        .builder()
                                        .RiskofHarm("riskOfHarm")
                                        .build())
                                .build())
                        .build()
                )
                .build();
    }


    @Test
    public void deliusRiskUpdateResponseIsTransformedCorrectly() {

        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.putObject("SubmitRiskDataResponse").put("CaseReferenceNumber", "A1234");


        DeliusRiskUpdateResponse deliusResponse = DeliusRiskUpdateResponse
                .builder()
                .body(root)
                .build();

        OasysRiskUpdateTransformer transformer = new OasysRiskUpdateTransformer();

        SubmitRiskDataResponseSoapEnvelope expected = SubmitRiskDataResponseSoapEnvelope
                .builder()
                .body(SubmitRiskDataResponseSoapBody
                        .builder()
                        .response(SubmitRiskDataResponse
                                .builder()
                                .caseReferenceNumber("A1234")
                                .header(Header
                                        .builder()
                                        .applicationMode("appMode")
                                        .correlationID("corrId")
                                        .messageTimestamp(NOW)
                                        .oasysRUsername("oasysRUsername")
                                        .build())
                                .build())
                        .build())
                .build();

        final SubmitRiskDataResponseSoapEnvelope actual = transformer.oasysRiskUpdateResponseOf(deliusResponse, Optional.of(anOasysRiskUpdate()));

        assertThat(actual).isEqualTo(expected);
    }

}