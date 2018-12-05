package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer.COURT_CODE_TYPE;
import static uk.gov.justice.digital.ndh.service.transforms.OffenderTransformer.SENTENCE_CODE_TYPE;

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
@DirtiesContext
public class OasysOffenderControllerTest3 {

//    public static final String GOOD_DELIUS_INITIAL_SEARCH_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchResponseFromDelius.xml")))
//            .lines().collect(Collectors.joining("\n"));
//    public static final String GOOD_DELIUS_OFFENDER_DETAILS_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsResponseFromDelius.xml")))
//            .lines().collect(Collectors.joining("\n"));
    private static final String REAL_DELIUS_RISK_FAULT_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/realFaultResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));
    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8090).asynchronousResponseEnabled(false).mappingSource(new JsonFileMappingsSource(new ClasspathFileSource("mappings"))).jettyStopTimeout(10000L));
    @LocalServerPort
    int port;
    @MockBean
    private MessageStoreService messageStoreService;
    @MockBean
    private ExceptionLogService exceptionLogService;
    @MockBean
    private MappingService mappingService;
    @MockBean
    private RequirementLookupRepository requirementLookupRepository;


    @Autowired
    private XmlMapper xmlMapper;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(1000L);
    }

    @Test
    public void postedOffenderDetailsMessageIsSentToDeliusAndHandledAppropriately() throws IOException {

//        stubFor(post(urlEqualTo("/delius/offenderDetails")).willReturn(
//                aResponse()
//                        .withBody(GOOD_DELIUS_OFFENDER_DETAILS_RESPONSE)
//                        .withStatus(200)));
//
//        Thread.sleep(4000L);
        when(mappingService.targetValueOf("MAG", COURT_CODE_TYPE)).thenReturn("MC");
        when(mappingService.targetValueOf("201", SENTENCE_CODE_TYPE)).thenReturn("910");
        when(requirementLookupRepository.findByReqTypeAndReqCodeAndSubCode("N", "X", "X02")).thenReturn(
                Optional.of(RequirementLookup.builder()
                        .activityDesc("Named Licenced Premises")
                        .reqType("N")
                        .reqCode("X")
                        .subCode("X02")
                        .sentenceAttributeCat("CJA_REQUIREMENT")
                        .sentenceAttributeElm("EXCLUSION")
                        .build()));

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = xmlMapper.readValue(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsResponseToOasys.xml")))
                .lines().collect(Collectors.joining("\n")), SoapEnvelopeSpec1_2.class);

        assertThat(actual).isEqualTo(expected);
    }
}