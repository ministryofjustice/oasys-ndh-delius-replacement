package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.api.oasys.request.Header;
import uk.gov.justice.digital.ndh.api.oasys.response.RiskUpdateResponse;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.service.EventsPullerService;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates",
        "ndelius.risk.update.url=http://localhost:8090/delius/riskUpdates",
        "ndelius.initial.search.url=http://localhost:8090/delius/initialSearch",
        "ndelius.offender.details.url=http://localhost:8090/delius/offenderDetails",
        "custody.api.base.url=http://localhost:8090/custodyapi/",
        "elite2.api.base.url=http://localhost:8090/elite2api/",
        "oasys.xtag.url=http://localhost:8090/oasys/",
        "oauth.url=http://localhost:8090/oauth/token"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("OASYS_LINKS")
@DirtiesContext
public class OasysAssessmentControllerTest {

    private static final String FAULT_GENERIC_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("wiremock/BadDeliusResponse.xml")))
            .lines().collect(Collectors.joining("\n"));
    private static final String REAL_DELIUS_RISK_FAULT_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/realFaultResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8090).asynchronousResponseEnabled(false).mappingSource(new JsonFileMappingsSource(new ClasspathFileSource("mappings"))).jettyStopTimeout(10000L));

    @LocalServerPort
    int port;
    @MockBean
    MappingService mappingService;
    @MockBean
    private MessageStoreService messageStoreService;
    @MockBean
    private ExceptionLogService exceptionLogService;
    @Autowired
    private XmlMapper xmlMapper;
    @Autowired
    private MBeanServer mBeanServer;
    @MockBean
    private EventsPullerService eventsPullerService;


    @Before
    public void setup() throws MalformedObjectNameException, MBeanException, ReflectionException {
        RestAssured.port = port;
        purgeMessageQueue();
        when(mappingService.descriptionOf(anyString(), anyLong())).thenReturn("description");
        when(mappingService.targetValueOf(anyString(), anyLong())).thenReturn("targetValue");
    }

    private void purgeMessageQueue() throws MBeanException, ReflectionException, MalformedObjectNameException {
        try {
            mBeanServer.invoke(ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=OASYS_MESSAGES"), "purge", new Object[]{}, new String[]{});
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void badAssessmentResponseFromDeliusIsLoggedAppropriately() throws InterruptedException {


        stubFor(post(urlEqualTo("/delius/assessmentUpdates")).willReturn(
                aResponse()
                        .withBody(FAULT_GENERIC_RESPONSE)
                        .withStatus(200)));

        Thread.sleep(2000L);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);

        Thread.sleep(1000L);

        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        Mockito.verify(exceptionLogService, times(1)).logFault(anyString(), anyString(), anyString());

    }

    @Test
    public void postedRiskMessageIsSentToDeliusAndHandledAppropriately() throws IOException {

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/oasysRiskRequestSoap.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysRiskUpdates")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = SoapEnvelopeSpec1_2.builder()
                .body(SoapBody.builder()
                        .riskUpdateResponse(RiskUpdateResponse.builder()
                                .caseReferenceNumber("T123456")
                                .header(Header.builder()
                                        .applicationMode("L")
                                        .correlationID("OASYSRPCN0220180726150731234567")
                                        .messageTimestamp("2018-07-26T15:00:31+01:00")
                                        .oasysRUsername("CN=Tony Gowland/OU=Northumbria/O=NPS")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void badRiskResponseFromDeliusIsLoggedAppropriately() throws InterruptedException {

        stubFor(post(urlEqualTo("/delius/riskUpdates")).willReturn(
                aResponse()
                        .withBody(REAL_DELIUS_RISK_FAULT_RESPONSE)
                        .withStatus(200)));

        Thread.sleep(2000L);


        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/oasysRiskRequestSoap.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysRiskUpdates")
                .then()
                .statusCode(200);

        System.out.println(Mockito.mockingDetails(exceptionLogService).getInvocations());

        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        Mockito.verify(exceptionLogService, times(1)).logFault(anyString(), anyString(), anyString());

    }

    @Test
    public void postedAssessmentMessageIsSentToDeliusAndHandledAppropriately() throws InterruptedException {
        Thread.sleep(1000);
        Mockito.reset(messageStoreService, exceptionLogService);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/AssessmentUpdates/OasysToNDHSoapEnvelope.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/oasysAssessments")
                .then()
                .log().all()
                .statusCode(200);

        Thread.sleep(3000);

        WireMock.verify(1, postRequestedFor(urlMatching("/delius/assessmentUpdates")));

        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        Mockito.verify(exceptionLogService, never()).logFault(anyString(), anyString(), anyString());

    }
}