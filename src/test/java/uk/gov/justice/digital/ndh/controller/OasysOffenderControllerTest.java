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
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_2;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookup;
import uk.gov.justice.digital.ndh.jpa.repository.RequirementLookupRepository;
import uk.gov.justice.digital.ndh.service.EventsPullerService;
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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
        "elite2.api.base.url=http://localhost:8090/elite2api/",
        "oasys.xtag.url=http://localhost:8090/oasys/",
        "oauth.url=http://localhost:8090/oauth/token"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysOffenderControllerTest {

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
    @MockBean
    private EventsPullerService eventsPullerService;


    @Autowired
    private XmlMapper xmlMapper;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
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
    public void mappingFailureInitialSearchResponseFromDeliusIsLoggedAppropriately() throws InterruptedException {
        Mockito.when(mappingService.descriptionOf(anyString(), anyLong())).thenThrow(NDHMappingException.builder().subject("description").value("sourceVal").code(0L).build());
        Mockito.when(mappingService.targetValueOf(anyString(), anyLong())).thenThrow(NDHMappingException.builder().subject("targetValue").value("sourceVal").code(0L).build());

        Thread.sleep(2000L);
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

        Mockito.when(mappingService.targetValueOf("CRD", 34L)).thenReturn("CRD");
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

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = xmlMapper.readValue("<?xml version='1.0' encoding='UTF-8'?><Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><Header/><Body><wstxns1:OffenderDetailsResponse xmlns:wstxns1=\"http://www.hp.com/NDH_Web_Service/Offender_Details_Response\"><wstxns1:Header><wstxns2:ApplicationMode xmlns:wstxns2=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">I</wstxns2:ApplicationMode><wstxns3:CorrelationID xmlns:wstxns3=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">OASYSRPCWWS20180910130951604609</wstxns3:CorrelationID><wstxns4:OASysRUsername xmlns:wstxns4=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">PCMS</wstxns4:OASysRUsername><wstxns5:MessageTimestamp xmlns:wstxns5=\"http://www.hp.com/NDH_Web_Service/DomainTypes\">2018-09-10T13:59:51+01:00</wstxns5:MessageTimestamp></wstxns1:Header><wstxns6:OffenderDetail xmlns:wstxns6=\"http://www.hp.com/NDH_Web_Service/offender\"><wstxns6:PrisonNumber>V32481</wstxns6:PrisonNumber><wstxns6:NomisID>G8696GH</wstxns6:NomisID><wstxns6:FamilyName>PIERCWYN</wstxns6:FamilyName><wstxns6:Forename1>DUPTCELSE</wstxns6:Forename1><wstxns6:Forename2>CHANDLEVIEVE</wstxns6:Forename2><wstxns6:Gender>1</wstxns6:Gender><wstxns6:DateOfBirth>1987-02-05</wstxns6:DateOfBirth><wstxns6:Alias><wstxns6:AliasFamilyName>HENRONZO</wstxns6:AliasFamilyName><wstxns6:AliasForename1>DUPTCELSE</wstxns6:AliasForename1><wstxns6:AliasDateOfBirth>1987-02-01</wstxns6:AliasDateOfBirth></wstxns6:Alias><wstxns6:PNC>01/395484N</wstxns6:PNC><wstxns6:CRONumber>333803/01K</wstxns6:CRONumber><wstxns6:Religion>NIL</wstxns6:Religion><wstxns6:ReleaseDate>2017-10-22</wstxns6:ReleaseDate><wstxns6:ReleaseType>CRD</wstxns6:ReleaseType><wstxns6:CellLocation>RNI-HB4-2-002</wstxns6:CellLocation><wstxns6:SecurityCategory>CAT-C</wstxns6:SecurityCategory><wstxns6:DischargeAddressLine2>ss</wstxns6:DischargeAddressLine2><wstxns6:DischargeAddressLine3>GlFNhbGlFNhb</wstxns6:DischargeAddressLine3><wstxns6:DischargeAddressLine4>HZpTHZp</wstxns6:DischargeAddressLine4><wstxns6:DischargeAddressLine5>Sheffield</wstxns6:DischargeAddressLine5><wstxns6:DischargeAddressLine6>S.YORKSHIRE</wstxns6:DischargeAddressLine6><wstxns6:AppealPendingIndicator>N</wstxns6:AppealPendingIndicator><wstxns6:LicenceExpiryDate>2019-07-04T00:00</wstxns6:LicenceExpiryDate><wstxns6:SentenceExpiryDate>2019-07-16T00:00</wstxns6:SentenceExpiryDate><wstxns6:ConditionalReleaseDate>2017-10-22T00:00</wstxns6:ConditionalReleaseDate><wstxns6:RiskOfSelfHarm>YES</wstxns6:RiskOfSelfHarm></wstxns6:OffenderDetail><wstxns7:EventDetail xmlns:wstxns7=\"http://www.hp.com/NDH_Web_Service/event\"><wstxns7:SentenceCode>920</wstxns7:SentenceCode><wstxns7:SentenceDate>2016-08-10</wstxns7:SentenceDate><wstxns7:SentenceLength>438</wstxns7:SentenceLength></wstxns7:EventDetail></wstxns1:OffenderDetailsResponse></Body></Envelope>", SoapEnvelopeSpec1_2.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DirtiesContext
    public void offenderNotFoundInNomisRespondsAppropriately() throws IOException {
        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml.replace("G8696GH", "A0000AA"))
                .post("/offenderDetails")
                .then()
                .statusCode(200).extract().body().asString();

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = xmlMapper.readValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://www.w3.org/2003/05/soap-envelope\"><SOAP-ENV:Body><SOAP-ENV:Fault><SOAP-ENV:Code><SOAP-ENV:Value>SOAP-ENV:NDH</SOAP-ENV:Value></SOAP-ENV:Code><SOAP-ENV:Reason><SOAP-ENV:Text xml:lang=\"en-US\">No offender details returned from NOMIS for offender - A0000AA</SOAP-ENV:Text></SOAP-ENV:Reason><SOAP-ENV:Node/><SOAP-ENV:Role/><SOAP-ENV:Detail><ns:Fault xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns=\"http://www.hp.com/NDH_Web_Service/Fault\" xmlns:ns0=\"http://www.w3.org/2003/05/soap-envelope\"><ns:BusinessException><ns:Code>NDH</ns:Code><ns:Description>No offender details returned from NOMIS for offender - A0000AA</ns:Description><ns:Timestamp>2018-10-01T14:22:06.641</ns:Timestamp><ns:RequestMessage>OASYSRPCWWS20180910130951604609</ns:RequestMessage></ns:BusinessException></ns:Fault></SOAP-ENV:Detail></SOAP-ENV:Fault></SOAP-ENV:Body></SOAP-ENV:Envelope>", SoapEnvelopeSpec1_2.class);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @DirtiesContext
    public void callsToCustodyAPIWillObtainToken() throws InterruptedException {

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n"));

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .statusCode(200);

        Thread.sleep(1000);

        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/oauth/token")).withBasicAuth(new BasicCredentials("none", "none")));
        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/custodyapi/offenders/nomsId/G8696GH")).withHeader("Authorization", new EqualToPattern("Bearer A.B.C")));
    }

    @Test
    @DirtiesContext
    public void callsToCustodyAPIWillReAuthenticate() throws InterruptedException {

        stubFor(get(urlPathEqualTo("/custodyapi/offenders/nomsId/Z0000ZZ"))
                .inScenario("expired_auth")
                .willReturn(aResponse().withStatus(401))
                .willSetStateTo("reauth"));

        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasysToNomis.xml")))
                .lines().collect(Collectors.joining("\n")).replace("G8696GH", "Z0000ZZ");

        final String offenderJson = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("__files/offenderZ0000ZZ.json")))
                .lines().collect(Collectors.joining("\n"));

        stubFor(get(urlPathEqualTo("/custodyapi/offenders/nomsId/Z0000ZZ"))
                .inScenario("expired_auth")
                .whenScenarioStateIs("reauth")
                .willReturn(aResponse().withStatus(200).withBody(offenderJson)));

        Thread.sleep(4000L);

        given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .statusCode(200);

        Thread.sleep(3000);

        WireMock.verify(2, postRequestedFor(urlPathEqualTo("/oauth/token")).withBasicAuth(new BasicCredentials("none", "none")));
        WireMock.verify(2, getRequestedFor(urlPathEqualTo("/custodyapi/offenders/nomsId/Z0000ZZ")).withHeader("Authorization", new EqualToPattern("Bearer A.B.C")));
    }

    @Test
    public void postedInitialSearchMessageIsSentToDeliusAndHandledAppropriately() throws IOException, InterruptedException {

        Mockito.when(mappingService.descriptionOf("328", 3802L)).thenReturn("ORA Youth Custody (inc PSS)");

        Thread.sleep(2000);
        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realInitialSearchRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/initialSearch")
                .then()
                .log().all()
                .statusCode(200).extract().body().asString();

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = xmlMapper.readValue(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/InitialSearch/realNdhInitialSearchResponseToOasys.xml")))
                .lines().collect(Collectors.joining("\n")), SoapEnvelopeSpec1_2.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void postedOffenderDetailsMessageIsSentToDeliusAndHandledAppropriately() throws IOException, InterruptedException {
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

        Thread.sleep(2000);
        final String requestXml = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsRequestFromOasys.xml")))
                .lines().collect(Collectors.joining("\n"));

        final String actualXml = given()
                .when()
                .contentType(ContentType.XML)
                .body(requestXml)
                .post("/offenderDetails")
                .then()
                .log().all()
                .statusCode(200).extract().body().asString();

        final SoapEnvelopeSpec1_2 actual = xmlMapper.readValue(actualXml, SoapEnvelopeSpec1_2.class);

        final SoapEnvelopeSpec1_2 expected = xmlMapper.readValue(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("xmls/OffenderDetails/realOffenderDetailsResponseToOasys.xml")))
                .lines().collect(Collectors.joining("\n")), SoapEnvelopeSpec1_2.class);

        assertThat(actual).isEqualTo(expected);
    }

}