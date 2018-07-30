package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeliusRiskUpdateClient {

    private final String ndeliusUrl;

    @Autowired
    public DeliusRiskUpdateClient(@Value("${ndelius.risk.update.url}") String ndeliusUrl) {
        this.ndeliusUrl = ndeliusUrl;
    }

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return Unirest.post(ndeliusUrl)
                .body(deliusSoapXml)
                .asString().getBody();
    }
}
