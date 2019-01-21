package uk.gov.justice.digital.ndh.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.ndh.service.transforms.CommonTransformer;

@Component
@Slf4j
public class OasysSOAPClient {

    private final String oasysUrl;
    private final String oasysUser;
    private final String oasysPassword;
    private final String hostName;
    private final String soapAction;
    private final CommonTransformer commonTransformer;

    @Autowired
    public OasysSOAPClient(@Value("${oasys.xtag.url}") String oasysUrl,
                           @Value(("${oasys.xtag.user:nouser}")) String oasysUser,
                           @Value("${oasys.xtag.password:nopassword}") String oasysPassword,
                           @Value("${oasys.xtag.hostname:nohostname}") String hostName,
                           @Value("${oasys.xtag.soapaction:nosoapaction}") String soapAction,
                           final CommonTransformer commonTransformer) {
        this.oasysUrl = oasysUrl;
        this.oasysUser = oasysUser;
        this.oasysPassword = oasysPassword;
        this.hostName = hostName;
        this.soapAction = soapAction;
        this.commonTransformer = commonTransformer;
    }

    public HttpResponse<String> oasysWebServiceResponseOf(String oasysSoapXml) throws UnirestException {

        log.info("Posting to {} with body beginning {}...", oasysUrl, commonTransformer.limitLength(oasysSoapXml,50));
        final RequestBodyEntity body = Unirest.post(oasysUrl)
                .basicAuth(oasysUser, oasysPassword)
                .header("Host", hostName)
                .header("SOAPAction", "//" + soapAction)
                .header("Content-Type", "application/soap+xml")
                .body(oasysSoapXml);

        final HttpResponse<String> stringHttpResponse = body.asString();
        log.info("... with response status {}", stringHttpResponse.getStatus());

        return stringHttpResponse;
    }
}
