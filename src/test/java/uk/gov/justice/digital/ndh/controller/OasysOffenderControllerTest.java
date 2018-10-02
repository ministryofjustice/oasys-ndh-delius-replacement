package uk.gov.justice.digital.ndh.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
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
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;
import uk.gov.justice.digital.ndh.service.MappingService;
import uk.gov.justice.digital.ndh.service.MessageStoreService;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
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
        "oauth.url=http://localhost:8090/oauth/token"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysOffenderControllerTest {

    public static final String GOOD_DELIUS_INITIAL_SEARCH_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));
    private static final String REAL_DELIUS_RISK_FAULT_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/RiskUpdate/realFaultResponseFromDelius.xml")))
            .lines().collect(Collectors.joining("\n"));
    public static final String GOOD_DELIUS_OFFENDER_DETAILS_RESPONSE = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsResponseFromDelius.xml")))
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
    @MockBean
    private RequirementLookupRepository requirementLookupRepository;


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
    public void postedOffenderDetailsMessageIsSentToDeliusAndHandledAppropriately() throws IOException {

        stubFor(post(urlEqualTo("/delius/offenderDetails")).willReturn(
                aResponse()
                        .withBody(GOOD_DELIUS_OFFENDER_DETAILS_RESPONSE)
                        .withStatus(200)));

        when(mappingService.targetValueOf("MAG", COURT_CODE_TYPE)).thenReturn("MC");
        when(mappingService.targetValueOf("201", SENTENCE_CODE_TYPE)).thenReturn("910");
        when(requirementLookupRepository.findByReqTypeAndReqCodeAndSubCode("N", "X","X02")).thenReturn(
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

        final SoapEnvelope actual = xmlMapper.readValue(actualXml, SoapEnvelope.class);

        final SoapEnvelope expected = xmlMapper.readValue(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsResponseToOasys.xml")))
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

    @Test
    public void postedOffenderDetailsMessageIsSentToCustodyAPIAndHandledAppropriately() throws IOException {
        wm.loadMappingsUsing(new JsonFileMappingsSource(new ClasspathFileSource("mappings")));

        assertThat(wm.getStubMappings().size()).isGreaterThan(0);

        Mockito.when(mappingService.targetValueOf("HDCAD", 34L)).thenReturn("HDC");
        Mockito.when(mappingService.targetValueOf("C", 13L)).thenReturn("CAT-C");

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelope actual = xmlMapper.readValue(actualXml, SoapEnvelope.class);

        final SoapEnvelope expected = xmlMapper.readValue("<?xml version='1.0' encoding='UTF-8'?><Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><Header/><Body><wstxns1:OffenderDetailsResponse xmlns:wstxns1=\"http://www.hp.com/NDH_Web_Service/Offender_Details_Response\"><wstxns2:OffenderDetail xmlns:wstxns2=\"http://www.hp.com/NDH_Web_Service/offender\"><wstxns2:PrisonNumber>V32481</wstxns2:PrisonNumber><wstxns2:NomisID>G8696GH</wstxns2:NomisID><wstxns2:FamilyName>PIERCWYN</wstxns2:FamilyName><wstxns2:Forename1>DUPTCELSE</wstxns2:Forename1><wstxns2:Forename2>CHANDLEVIEVE</wstxns2:Forename2><wstxns2:Gender>1</wstxns2:Gender><wstxns2:DateOfBirth>1987-02-05</wstxns2:DateOfBirth><wstxns2:Alias><wstxns2:AliasFamilyName>HENRONZO</wstxns2:AliasFamilyName><wstxns2:AliasForename1>DUPTCELSE</wstxns2:AliasForename1><wstxns2:AliasDateOfBirth>1987-02-01</wstxns2:AliasDateOfBirth></wstxns2:Alias><wstxns2:PNC>01/395484N</wstxns2:PNC><wstxns2:CRONumber>333803/01K</wstxns2:CRONumber><wstxns2:Religion>NIL</wstxns2:Religion><wstxns2:ReleaseDate>2017-10-22</wstxns2:ReleaseDate><wstxns2:ReleaseType>HDC</wstxns2:ReleaseType><wstxns2:CellLocation>RNI-HB4-2-002</wstxns2:CellLocation><wstxns2:SecurityCategory>CAT-C</wstxns2:SecurityCategory><wstxns2:DischargeAddressLine2>ss</wstxns2:DischargeAddressLine2><wstxns2:DischargeAddressLine3>GlFNhbGlFNhb</wstxns2:DischargeAddressLine3><wstxns2:DischargeAddressLine4>HZpTHZp</wstxns2:DischargeAddressLine4><wstxns2:DischargeAddressLine5>Sheffield</wstxns2:DischargeAddressLine5><wstxns2:DischargeAddressLine6>S.YORKSHIRE</wstxns2:DischargeAddressLine6><wstxns2:AppealPendingIndicator>N</wstxns2:AppealPendingIndicator><wstxns2:LicenceExpiryDate>2019-07-04T00:00</wstxns2:LicenceExpiryDate><wstxns2:SentenceExpiryDate>2019-07-16T00:00</wstxns2:SentenceExpiryDate><wstxns2:ConditionalReleaseDate>2017-10-22T00:00</wstxns2:ConditionalReleaseDate><wstxns2:RiskOfSelfHarm>YES</wstxns2:RiskOfSelfHarm></wstxns2:OffenderDetail><wstxns3:EventDetail xmlns:wstxns3=\"http://www.hp.com/NDH_Web_Service/event\"><wstxns3:SentenceCode>920</wstxns3:SentenceCode><wstxns3:SentenceDate>2016-08-10</wstxns3:SentenceDate><wstxns3:SentenceLength>438</wstxns3:SentenceLength></wstxns3:EventDetail></wstxns1:OffenderDetailsResponse></Body></Envelope>", SoapEnvelope.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void offenderNotFoundInNomisRespondsAppropriately() throws IOException {
        wm.loadMappingsUsing(new JsonFileMappingsSource(new ClasspathFileSource("mappings")));

        assertThat(wm.getStubMappings().size()).isGreaterThan(0);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml.replace("G8696GH", "A0000AA"))
                .post("/offenderDetails")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelope actual = xmlMapper.readValue(actualXml, SoapEnvelope.class);

        final SoapEnvelope expected = xmlMapper.readValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://www.w3.org/2003/05/soap-envelope\"><SOAP-ENV:Body><SOAP-ENV:Fault><SOAP-ENV:Code><SOAP-ENV:Value>SOAP-ENV:NDH</SOAP-ENV:Value></SOAP-ENV:Code><SOAP-ENV:Reason><SOAP-ENV:Text xml:lang=\"en-US\">No offender details returned from NOMIS for offender - A0000AA</SOAP-ENV:Text></SOAP-ENV:Reason><SOAP-ENV:Node/><SOAP-ENV:Role/><SOAP-ENV:Detail><ns:Fault xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns=\"http://www.hp.com/NDH_Web_Service/Fault\" xmlns:ns0=\"http://www.w3.org/2003/05/soap-envelope\"><ns:BusinessException><ns:Code>NDH</ns:Code><ns:Description>No offender details returned from NOMIS for offender - A0000AA</ns:Description><ns:Timestamp>2018-10-01T14:22:06.641</ns:Timestamp><ns:RequestMessage>OASYSRPCWWS20180910130951604609</ns:RequestMessage></ns:BusinessException></ns:Fault></SOAP-ENV:Detail></SOAP-ENV:Fault></SOAP-ENV:Body></SOAP-ENV:Envelope>", SoapEnvelope.class);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void callsToCustodyAPIWillObtainToken() {
        wm.loadMappingsUsing(new JsonFileMappingsSource(new ClasspathFileSource("mappings")));

        assertThat(wm.getStubMappings().size()).isGreaterThan(0);

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .statusCode(200);


        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/oauth/token")).withBasicAuth(new BasicCredentials("none","none")));
        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/custodyapi/offenders/nomsId/G8696GH")).withHeader("Authorization", new EqualToPattern("Bearer A.B.C")));
    }

}