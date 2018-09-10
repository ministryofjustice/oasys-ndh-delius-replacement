package uk.gov.justice.digital.ndh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.digital.ndh.service.DeliusSOAPClient;

@Configuration
public class DeliusClientConfig {

    @Value("${ndelius.webservice.user:nouser}")
    private String ndeliusUser;

    @Value("${ndelius.webservice.password:nopassword}")
    private String ndeliusPassword;

    @Value("${ndelius.hostname:hostname}")
    private String hostName;

    @Bean
    public DeliusSOAPClient assessmentUpdateClient(@Value("${ndelius.assessment.update.url}") String ndeliusUrl,
                                                   @Value("${ndelius.assessment.update.soapAction:submitAssessmentSummary}") String soapAction) {
        return DeliusSOAPClient.builder()
                .hostName(hostName)
                .ndeliusPassword(ndeliusPassword)
                .ndeliusUrl(ndeliusUrl)
                .ndeliusUser(ndeliusUser)
                .soapAction(soapAction)
                .build();
    }

    @Bean
    public DeliusSOAPClient initialSearchClient(@Value("${ndelius.initial.search.url}") String ndeliusUrl,
                                                @Value("${ndelius.initial.search.soapAction:getSubSetOffenderDetails}") String soapAction) {
        return DeliusSOAPClient.builder()
                .hostName(hostName)
                .ndeliusPassword(ndeliusPassword)
                .ndeliusUrl(ndeliusUrl)
                .ndeliusUser(ndeliusUser)
                .soapAction(soapAction)
                .build();
    }

    @Bean
    public DeliusSOAPClient offenderDetailsClient(@Value("${ndelius.offender.details.url}") String ndeliusUrl,
                                                  @Value("${ndelius.initial.search.soapAction:getOffenderDetails}") String soapAction) {
        return DeliusSOAPClient.builder()
                .hostName(hostName)
                .ndeliusPassword(ndeliusPassword)
                .ndeliusUrl(ndeliusUrl)
                .ndeliusUser(ndeliusUser)
                .soapAction(soapAction)
                .build();
    }

    @Bean
    public DeliusSOAPClient riskUpdateClient(@Value("${ndelius.risk.update.url}") String ndeliusUrl,
                                             @Value("${ndelius.initial.search.soapAction:submitRiskData}") String soapAction) {
        return DeliusSOAPClient.builder()
                .hostName(hostName)
                .ndeliusPassword(ndeliusPassword)
                .ndeliusUrl(ndeliusUrl)
                .ndeliusUser(ndeliusUser)
                .soapAction(soapAction)
                .build();
    }

}
