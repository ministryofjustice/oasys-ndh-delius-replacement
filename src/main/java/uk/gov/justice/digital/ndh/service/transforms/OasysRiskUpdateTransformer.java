package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;

import java.util.Optional;

@Service
public class OasysRiskUpdateTransformer {

    private final FaultTransformer faultTransformer;
    private final CommonTransformer commonTransformer;

    @Autowired
    public OasysRiskUpdateTransformer(FaultTransformer faultTransformer, CommonTransformer commonTransformer, XmlMapper xmlMapper) {
        this.faultTransformer = faultTransformer;
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
                                        .riskOfHarm(oasysRiskUpdate.getBody().getRiskUpdateRequest().getRisk().getRiskofHarm())
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

    public String stringResponseOf(DeliusRiskUpdateResponse response, Optional<SoapEnvelope> maybeOasysRiskUpdate, Optional<String> rawDeliusResponse) throws DocumentException, JsonProcessingException {
        final String correlationID = maybeOasysRiskUpdate.get().getBody().getRiskUpdateRequest().getHeader().getCorrelationID();
        if (response.isSoapFault()) {
            return faultTransformer.oasysFaultResponseOf(rawDeliusResponse.get(), correlationID);
        } else {
            final SoapEnvelope transformedResponse = oasysRiskUpdateResponseOf(response, maybeOasysRiskUpdate);
            return commonTransformer.transformedResponseXmlOf(transformedResponse);
        }
    }
}
