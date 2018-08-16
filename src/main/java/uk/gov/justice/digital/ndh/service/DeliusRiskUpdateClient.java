package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeliusRiskUpdateClient {

    private final String ndeliusUrl;
    private final String ndeliusUser;
    private final String ndeliusPassword;
    private final String hostName;

    @Autowired
    public DeliusRiskUpdateClient(@Value("${ndelius.risk.update.url}") String ndeliusUrl,
                                  @Value("${ndelius.webservice.user:nouser}") String ndeliusUser,
                                  @Value("${ndelius.webservice.password:nopassword}") String ndeliusPassword,
                                  @Value("${ndelius.hostname:oasys400.noms.gsi.gov.uk'}") String hostName) {
        this.ndeliusUrl = ndeliusUrl;
        this.ndeliusUser = ndeliusUser;
        this.ndeliusPassword = ndeliusPassword;
        this.hostName = hostName;
    }

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return Unirest.post(ndeliusUrl)
                .basicAuth(ndeliusUser, ndeliusPassword)
                .header("Host", hostName)
                .header("SOAPAction","/NDeliusOASYS/SubmitRiskData")
                .header("Content-Type","application/soap+xml")
                .body(deliusSoapXml)
                .asString().getBody();
    }
}
