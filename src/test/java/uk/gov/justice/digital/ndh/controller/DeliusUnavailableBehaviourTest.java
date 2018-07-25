package uk.gov.justice.digital.ndh.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.jpa.repository.ExceptionLogRepository;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;
import uk.gov.justice.digital.ndh.service.DeliusClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.timeout;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class DeliusUnavailableBehaviourTest {

    @LocalServerPort
    int port;
    public static final String GOOD_RESPONSE_XML = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("__files/GoodDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));

    @Autowired
    private MessageStoreRepository messageStoreRepository;
    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    @MockBean
    private DeliusClient deliusClient;

    @Before
    public void setup() throws UnirestException {
        RestAssured.port = port;
        Mockito.when(deliusClient.deliusWebServiceResponseOf(any(String.class))).thenThrow(new UnirestException("unreachable"));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void messagesAreReplayedWhenDeliusIsUnavailableButOnlyLoggedOnce() {


        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        ClassLoader.getSystemResourceAsStream("xmls/OasysToNDHSoapEnvelope.xml");

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);

        try {
            Mockito.verify(deliusClient, timeout(10000).atLeast(5)).deliusWebServiceResponseOf(anyString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        // Messages are logged before and after transformation, so check this happens only once
        assertThat(messageStoreRepository.count()).isEqualTo(2L);
        // Exception is logged on the initial failure to talk to Delius, ensure only the first one is logged.
        assertThat(exceptionLogRepository.count()).isEqualTo(1L);

    }
}