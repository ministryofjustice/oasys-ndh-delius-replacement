package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OasysSOAPClient {

    private final String oasysUrl;
    private final String oasysUser;
    private final String oasysPassword;
    private final String hostName;
    private final String soapAction;

    public OasysSOAPClient(@Value("${oasys.xtag.url}") String oasysUrl,
                           @Value(("${oasys.xtag.user:nouser}")) String oasysUser,
                           @Value("${oasys.xtag.password:nopassword}") String oasysPassword,
                           @Value("${oasys.xtag.hostname:nohostname}") String hostName,
                           @Value("${oasys.xtag.soapaction:nosoapaction}") String soapAction) {
        this.oasysUrl = oasysUrl;
        this.oasysUser = oasysUser;
        this.oasysPassword = oasysPassword;
        this.hostName = hostName;
        this.soapAction = soapAction;
    }

    public HttpResponse<String> oasysWebServiceResponseOf(String oasysSoapXml) throws UnirestException {
        return Unirest.post(oasysUrl)
                .basicAuth(oasysUser, oasysPassword)
                .header("Host", hostName)
                .header("SOAPAction", "//" + soapAction)
                .header("Content-Type", "application/soap+xml")
                .body(oasysSoapXml).asString();
    }
}
