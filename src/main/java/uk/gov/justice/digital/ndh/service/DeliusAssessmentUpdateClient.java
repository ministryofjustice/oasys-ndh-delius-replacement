package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeliusAssessmentUpdateClient {

    private final String ndeliusUrl;
    private final String ndeliusUser;
    private final String ndeliusPassword;

    @Autowired
    public DeliusAssessmentUpdateClient(@Value("${ndelius.assessment.update.url}") String ndeliusUrl,
                                        @Value("${ndelius.webservice.user:nouser}") String ndeliusUser,
                                        @Value("${ndelius.webservice.password:nopassword}") String ndeliusPassword) {
        this.ndeliusUrl = ndeliusUrl;
        this.ndeliusUser = ndeliusUser;
        this.ndeliusPassword = ndeliusPassword;
    }

    public String deliusWebServiceResponseOf(String deliusSoapXml) throws UnirestException {
        return Unirest.post(ndeliusUrl)
                .basicAuth(ndeliusUser,ndeliusPassword)
                .body(deliusSoapXml)
                .asString().getBody();
    }
}
