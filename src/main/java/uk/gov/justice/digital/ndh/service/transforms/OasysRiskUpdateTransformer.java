package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.javafx.binding.StringFormatter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import uk.gov.justice.digital.ndh.api.delius.request.RiskType;
import uk.gov.justice.digital.ndh.api.delius.request.SubmitRiskDataRequest;
import uk.gov.justice.digital.ndh.api.delius.response.DeliusRiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

@Service
public class OasysRiskUpdateTransformer {

    public static final String VERSION = "1.0";
    public static final String NDH_NAMESPACE_URI = "http://www.hp.com/NDH_Web_Service/Fault";
    public static final String BCL_NAMESPACE_URI = "http://www.bconline.co.uk/oasys/fault";
    public static final String NDH_NS_PREFIX = "ndh";
    public static final Namespace NDH_NAMESPACE = new Namespace(NDH_NS_PREFIX, NDH_NAMESPACE_URI);
    public static final String NDH = "NDH";
    public static final String NDH_ERROR = "PCMS Web Service has returned an error";
    private final XmlMapper xmlMapper;

    @Autowired
    public OasysRiskUpdateTransformer(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    public SoapEnvelope deliusRiskUpdateRequestOf(SoapEnvelope oasysRiskUpdate) {
        return SoapEnvelope.builder()
                .header(SoapHeader
                        .builder()
                        .header(uk.gov.justice.digital.ndh.api.delius.request.Header
                                .builder()
                                .messageId(oasysRiskUpdate.getBody().getRiskUpdateRequest().getHeader().getCorrelationID())
                                .version(VERSION)
                                .build())
                        .build())
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


    public String oasysFaultResponseOf(String deliusFaultResponse, String correlationId) throws DocumentException {
        SAXReader reader = new SAXReader();
        org.dom4j.Document document = reader.read(new InputSource(new StringReader(deliusFaultResponse)));

        final String soapPrefix = document.getRootElement().getNamespace().getPrefix();

        transformFaultDetail(correlationId, document, soapPrefix);

        transformFaultReason(document, soapPrefix);

        transformFaultCode(document, soapPrefix);

        return document.asXML();
    }

    private void transformFaultCode(Document document, String soapPrefix) {
        final String faultCodeValueXpath = StringFormatter.format("//%s:Envelope/%s:Body/%s:Fault/%s:Code/%s:Value", soapPrefix, soapPrefix, soapPrefix, soapPrefix, soapPrefix).getValue();

        Node faultCodeValueNode = document.selectSingleNode(faultCodeValueXpath);

        faultCodeValueNode.setText(String.format("%s:%s", soapPrefix, NDH));
    }

    private void transformFaultReason(Document document, String soapPrefix) {
        final String faultReasonTextXpath = StringFormatter.format("//%s:Envelope/%s:Body/%s:Fault/%s:Reason/%s:Text", soapPrefix, soapPrefix, soapPrefix, soapPrefix, soapPrefix).getValue();

        Node faultCodeValueNode = document.selectSingleNode(faultReasonTextXpath);

        faultCodeValueNode.setText(String.format("%s:%s", soapPrefix, NDH_ERROR));
    }

    private void transformFaultDetail(String correlationId, Document document, String soapPrefix) {
        final String faultDetailXpath = StringFormatter.format("//%s:Envelope/%s:Body/%s:Fault/%s:Detail", soapPrefix, soapPrefix, soapPrefix, soapPrefix).getValue();

        Node detailNode = document.selectSingleNode(faultDetailXpath);

        final List<Node> childNodes = detailNode.selectNodes(".//*");

        childNodes.forEach(node -> {
            transformNode(node, correlationId);
        });
    }

    private void transformNode(Node node, String correlationId) {

        transformNodeNamespace(node);

        if (node.getName().equals("RequestMessage")) {
            node.setText(correlationId);
        }
    }

    private void transformNodeNamespace(Node node) {
        if (node instanceof DefaultElement) {
            final DefaultElement defaultElement = (DefaultElement) node;
            final Namespace namespace = defaultElement.getNamespace();
            if (namespace.getURI().equals(BCL_NAMESPACE_URI)) {
                defaultElement.remove(namespace);
                defaultElement.setNamespace(NDH_NAMESPACE);
            }
        }
    }

    public String stringResponseOf(DeliusRiskUpdateResponse response, Optional<SoapEnvelope> maybeOasysRiskUpdate, Optional<String> rawDeliusResponse) throws DocumentException, JsonProcessingException {
        final String correlationID = maybeOasysRiskUpdate.get().getBody().getRiskUpdateRequest().getHeader().getCorrelationID();
        if (response.isSoapFault()) {
            return oasysFaultResponseOf(rawDeliusResponse.get(), correlationID);
        } else {
            final SoapEnvelope transformedResponse = oasysRiskUpdateResponseOf(response, maybeOasysRiskUpdate);
            return transformedResponseXmlOf(transformedResponse);

        }
    }

    private String transformedResponseXmlOf(SoapEnvelope transformedResponse) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(transformedResponse);
    }

}
