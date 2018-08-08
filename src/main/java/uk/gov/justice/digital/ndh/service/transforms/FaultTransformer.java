package uk.gov.justice.digital.ndh.service.transforms;

import com.sun.javafx.binding.StringFormatter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.List;

@Component
public class FaultTransformer {

    public static final String NDH_NAMESPACE_URI = "http://www.hp.com/NDH_Web_Service/Fault";
    public static final String NDH_NS_PREFIX = "ndh";
    public static final Namespace NDH_NAMESPACE = new Namespace(NDH_NS_PREFIX, NDH_NAMESPACE_URI);
    public static final String BCL_NAMESPACE_URI = "http://www.bconline.co.uk/oasys/fault";
    public static final String NDH = "NDH";
    public static final String NDH_ERROR = "PCMS Web Service has returned an error";


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
}
