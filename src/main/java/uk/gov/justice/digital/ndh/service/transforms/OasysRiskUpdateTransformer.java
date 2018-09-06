package uk.gov.justice.digital.ndh.service.transforms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;

import java.util.Optional;
import java.util.function.Function;

@Service
public class OasysRiskUpdateTransformer {

    private final CommonTransformer commonTransformer;
    public static final Function<String, String> deliusRiskFlagOf = part -> "".equals(part) ? "N" : part;

    @Autowired
    public OasysRiskUpdateTransformer(CommonTransformer commonTransformer) {
        this.commonTransformer = commonTransformer;
    }

    public SoapEnvelope deliusRiskUpdateRequestOf(SoapEnvelope oasysRiskUpdate) {
        final String correlationID = oasysRiskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID();

        return SoapEnvelope.builder()
                .header(commonTransformer.deliusSoapHeaderOf(correlationID))
                .body(SoapBody
                        .builder()
                        .submitRiskDataRequest(SubmitRiskDataRequest
                                .builder()
                                .risk(RiskType
                                        .builder()
                                        .riskOfHarm(commonTransformer.deliusRiskFlagsOf(oasysRiskUpdate.getBody().getRiskUpdateRequest().getRiskFlags(), deliusRiskFlagOf))
                                        .caseReferenceNumber(oasysRiskUpdate.getBody().getRiskUpdateRequest().getCmsProbNumber())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public SoapEnvelope oasysRiskUpdateResponseOf(DeliusRiskUpdateResponse deliusRiskUpdateResponse, Optional<SoapEnvelope> maybeOasysRiskUpdate) {
        return SoapEnvelope
                .builder()
                .body(SoapBody
                        .builder()
                        .riskUpdateResponse(RiskUpdateResponse
                                .builder()
                                .caseReferenceNumber(deliusRiskUpdateResponse.getCaseReferenceNumber().orElse(null))
                                .header(maybeOasysRiskUpdate.map(soapEnvelope -> soapEnvelope.getBody().getRiskUpdateRequest().getHeader()).orElse(null))
                                .build())
                        .build())
                .build();
    }

}
