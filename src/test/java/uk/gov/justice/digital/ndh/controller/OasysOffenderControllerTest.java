package uk.gov.justice.digital.ndh.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates",
        "ndelius.risk.update.url=http://localhost:8090/delius/riskUpdates",
        "ndelius.initial.search.url=http://localhost:8090/delius/initialSearch"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysOffenderControllerTest {

    public static final String GOOD_RESPONSE_XML = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("wiremock/GoodDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));
    public static final String BAD_RESPONSE_XML = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("wiremock/BadDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8090));

    @LocalServerPort
    int port;
    @MockBean
    private MessageStoreService messageStoreService;
    @MockBean
    private ExceptionLogService exceptionLogService;

    @Autowired
    private MBeanServer mBeanServer;

    @Before
    public void setup() throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        RestAssured.port = port;
        purgeMessageQueue();
    }

    private Object purgeMessageQueue() throws InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException {
        return mBeanServer.invoke(ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=OASYS_MESSAGES"), "purge",new Object[]{}, new String[]{});
    }

    @After
    public void tearDown() {

    }

//    @Test
//    public void postedXmlMessageIsSentToDeliusAndHandledAppropriately() throws InterruptedException {
//
//        stubFor(post(urlEqualTo("/delius/assessmentUpdates")).willReturn(
//                aResponse()
//                        .withBody(NON_FAULT_GENERIC_RESPONSE)
//                        .withStatus(200)));
//
//        // Annoying: Stub seems to take time to register for some reason
//        Thread.sleep(2000);
//
//        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
//                .lines().collect(Collectors.joining("\n"));
//
//        ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml");
//
//        given()
//                .when()
//                .contentType(ContentType.XML)
//                .body(requestXml)
//                .post("/oasysAssessments")
//                .then()
//                .statusCode(200);
//
//        await().atMost(10, TimeUnit.SECONDS).until(() -> {
//            try {
//                wm.verify(1, postRequestedFor(urlMatching("/delius/assessmentUpdates")));
//                return true;
//            } catch (Throwable t) {
//                return false;
//            }
//        });
//
//        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), any(MessageStoreService.ProcStates.class));
//        Mockito.verify(exceptionLogService, never()).logFault(anyString(), anyString(), anyString());
//
//    }
//
//    @Test
//    public void badResponseFromDeliusIsLoggedAppropriately() throws InterruptedException {
//
//        stubFor(post(urlEqualTo("/delius/assessmentUpdates")).willReturn(
//                aResponse()
//                        .withBody(FAULT_GENERIC_RESPONSE)
//                        .withStatus(200)));
//
//        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
//                .lines().collect(Collectors.joining("\n"));
//
//        ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml");
//
//        given()
//                .when()
//                .contentType(ContentType.XML)
//                .body(requestXml)
//                .post("/oasysAssessments")
//                .then()
//                .statusCode(200);
//
//        Thread.sleep(1000);
//
//        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), any(MessageStoreService.ProcStates.class));
//        Mockito.verify(exceptionLogService, times(1)).logFault(anyString(), anyString(), anyString());
//
//    }
}