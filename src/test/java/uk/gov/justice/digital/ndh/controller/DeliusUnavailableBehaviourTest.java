package uk.gov.justice.digital.ndh.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.service.DeliusAssessmentUpdateClient;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates",
        "ndelius.risk.update.url=http://localhost:8090/delius/riskUpdates"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class DeliusUnavailableBehaviourTest {

    @LocalServerPort
    int port;

    @MockBean
    private ExceptionLogService exceptionLogService;

    @MockBean
    private MessageStoreService messageStoreService;

    @MockBean
    private DeliusAssessmentUpdateClient deliusAssessmentUpdateClient;

    @Before
    public void setup() throws UnirestException {
        RestAssured.port = port;
        Mockito.when(deliusAssessmentUpdateClient.deliusWebServiceResponseOf(any(String.class))).thenThrow(new UnirestException("unreachable"));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void messagesAreReplayedWhenDeliusIsUnavailableButOnlyLoggedOnce() {


        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);

        try {
            Mockito.verify(deliusAssessmentUpdateClient, timeout(20000).atLeast(5)).deliusWebServiceResponseOf(anyString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        // Messages are logged before and after transformation, so check this happens only once
        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        // Exception is logged on the initial failure to talk to Delius, ensure only the first one is logged.
        Mockito.verify(exceptionLogService, times(1)).logFault(anyString(), anyString(), anyString());
    }
}