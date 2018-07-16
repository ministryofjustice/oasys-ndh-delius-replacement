package uk.gov.justice.digital.ndh.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.jmx.enabled=true")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class OasysAssessmentControllerTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void postedXmlMessageIsHandledAppropriately() throws IOException {

        final InputStream sampleXmlStream = ClassLoader.getSystemResourceAsStream("xmls/OasysToNDH.xml");

        given()
                .when()
                .contentType(ContentType.XML)
                .body(sampleXmlStream)
                .post("/oasysAssessments")
                .then()
                .statusCode(200);
    }
}