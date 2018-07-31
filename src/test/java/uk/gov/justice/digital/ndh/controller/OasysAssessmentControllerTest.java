package uk.gov.justice.digital.ndh.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.jpa.entity.ExceptionLog;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.repository.ExceptionLogRepository;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates",
        "ndelius.risk.update.url=http://localhost:8090/delius/riskUpdates"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysAssessmentControllerTest {

    public static final String GOOD_RESPONSE_XML = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("__files/GoodDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));
    public static final String BAD_RESPONSE_XML = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("__files/BadDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8090));

    @LocalServerPort
    int port;
    @MockBean
    private MessageStoreRepository messageStoreRepository;
    @MockBean
    private ExceptionLogRepository exceptionLogRepository;

    @Autowired
    private MBeanServer mBeanServer;

    @Before
    public void setup() throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        RestAssured.port = port;
        purgeMessageQueue();
        Mockito.when(messageStoreRepository.save(Mockito.any(MsgStore.class))).thenReturn(MsgStore.builder().build());
        Mockito.when(exceptionLogRepository.save(Mockito.any(ExceptionLog.class))).thenReturn(ExceptionLog.builder().build());
        reset(messageStoreRepository, exceptionLogRepository);
    }

    private Object purgeMessageQueue() throws InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException {
        return mBeanServer.invoke(ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=OASYS_MESSAGES"), "purge",new Object[]{}, new String[]{});
    }

    @After
    public void tearDown() {

    }

    @Test
    public void postedXmlMessageIsSentToDeliusAndHandledAppropriately() throws InterruptedException {

        stubFor(post(urlEqualTo("/delius/assessmentUpdates")).willReturn(
                aResponse()
                        .withBody(GOOD_RESPONSE_XML)
                        .withStatus(200)));

        // Annoying: Stub seems to take time to register for some reason
        Thread.sleep(5000);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml");

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            try {
                wm.verify(1, postRequestedFor(urlMatching("/delius/assessmentUpdates")));
                return true;
            } catch (Throwable t) {
                return false;
            }
        });

        Mockito.verify(messageStoreRepository, times(2)).save(Matchers.any(MsgStore.class));
        Mockito.verify(exceptionLogRepository, never()).save(Matchers.any(ExceptionLog.class));


    }

    @Test
    public void badResponseFromDeliusIsLoggedAppropriately() throws InterruptedException {

        stubFor(post(urlEqualTo("/delius/assessmentUpdates")).willReturn(
                aResponse()
                        .withBody(BAD_RESPONSE_XML)
                        .withStatus(200)));

        // Annoying: Stub seems to take time to register for some reason
        Thread.sleep(5000);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml");

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);

        Thread.sleep(1000);

        Mockito.verify(messageStoreRepository, times(2)).save(Matchers.any(MsgStore.class));
        Mockito.verify(exceptionLogRepository, times(1)).save(Matchers.any(ExceptionLog.class));

    }
}