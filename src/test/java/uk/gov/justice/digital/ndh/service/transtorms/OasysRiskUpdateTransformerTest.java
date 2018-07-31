package uk.gov.justice.digital.ndh.service.transtorms;

import org.junit.Test;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapBody;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapEnvelope;
import uk.gov.justice.digital.ndh.api.delius.request.DeliusRiskUpdateSoapHeader;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.request.Risk;
import uk.gov.justice.digital.ndh.api.oasys.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class OasysRiskUpdateTransformerTest {

    @Test
    public void oasysRiskUpdateIsTransformedCorrectly() {
        final String now = LocalDateTime.now().toString();

        final SoapEnvelope oasysRequest = SoapEnvelope
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
                                        .messageTimestamp(now)
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


    @Test
    public void deliusRiskUpdateResponseIsTransformedCorrectly() {


    }

}