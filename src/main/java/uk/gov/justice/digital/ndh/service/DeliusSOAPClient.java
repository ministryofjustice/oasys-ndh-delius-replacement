package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.Builder;

@Builder
public class DeliusSOAPClient {

    private final String ndeliusUrl;
    private final String ndeliusUser;
    private final String ndeliusPassword;
    private final String hostName;
    private final String soapAction;

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return Unirest.post(ndeliusUrl)
                .basicAuth(ndeliusUser, ndeliusPassword)
                .header("Host", hostName)
                .header("SOAPAction", "/NDeliusOASYS/" + soapAction)
                .header("Content-Type", "application/soap+xml")
                .body(deliusSoapXml)
                .asString().getBody();
    }
}
