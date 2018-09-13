package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelope;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jmx.enabled=true",
        "ndelius.assessment.update.url=http://localhost:8090/delius/assessmentUpdates",
        "ndelius.risk.update.url=http://localhost:8090/delius/riskUpdates",
        "ndelius.initial.search.url=http://localhost:8090/delius/initialSearch",
        "ndelius.offender.details.url=http://localhost:8090/delius/offenderDetails"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysOffenderControllerTest {

    public static final String GOOD_DELIUS_INITIAL_SEARCH_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));
    private static final String REAL_DELIUS_RISK_FAULT_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/realFaultResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8090));

    @LocalServerPort
    int port;
    @MockBean
    private MessageStoreService messageStoreService;
    @MockBean
    private ExceptionLogService exceptionLogService;
    @MockBean
    private MappingService mappingService;


    @Autowired
    private XmlMapper xmlMapper;

    @Before
    public void setup() throws InterruptedException {
        RestAssured.port = port;
        Thread.sleep(5000L);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(5000L);
    }

    @Test
    public void postedInitialSearchMessageIsSentToDeliusAndHandledAppropriately() throws IOException {

        stubFor(post(urlEqualTo("/delius/initialSearch")).willReturn(
                aResponse()
                        .withBody(GOOD_DELIUS_INITIAL_SEARCH_RESPONSE)
                        .withStatus(200)));

        Mockito.when(mappingService.descriptionOf("328", 3802L)).thenReturn("ORA Youth Custody (inc PSS)");

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/initialSearch")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelope actual = xmlMapper.readValue(actualXml, SoapEnvelope.class);

        final SoapEnvelope expected = xmlMapper.readValue(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realNdhInitialSearchResponseToOasys.xml")))
                .lines().collect(Collectors.joining("\n")), SoapEnvelope.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void badRiskResponseFromDeliusIsLoggedAppropriately() {


        stubFor(post(urlEqualTo("/delius/initialSearch")).willReturn(
                aResponse()
                        .withBody(REAL_DELIUS_RISK_FAULT_RESPONSE)
                        .withStatus(200)));

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/initialSearch")
                .then()
                .statusCode(200);

        Mockito.verify(messageStoreService, times(2)).writeMessage(anyString(), anyString(), anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        Mockito.verify(exceptionLogService, times(1)).logFault(anyString(), anyString(), anyString());

    }

    @Test
    public void mappingFailureInitialSearchResponseFromDeliusIsLoggedAppropriately() {


        stubFor(post(urlEqualTo("/delius/initialSearch")).willReturn(
                aResponse()
                        .withBody(GOOD_DELIUS_INITIAL_SEARCH_RESPONSE)
                        .withStatus(200)));

        Mockito.when(mappingService.descriptionOf(anyString(), anyLong())).thenThrow(NDHMappingException.builder().subject("description").value("sourceVal").code(0L).build());
        Mockito.when(mappingService.targetValueOf(anyString(), anyLong())).thenThrow(NDHMappingException.builder().subject("targetValue").value("sourceVal").code(0L).build());

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String s = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/initialSearch")
                .then()
                .statusCode(200).extract().body().asString();

        assertThat(s).contains("PCMS mapping error");
        Mockito.verify(messageStoreService, times(3)).writeMessage(anyString(), anyString(), anyString(), anyString(), any(MessageStoreService.ProcStates.class));
        Mockito.verify(exceptionLogService, times(1)).logMappingFail(anyLong(), anyString(), anyString(), anyString(), anyString());

    }

}