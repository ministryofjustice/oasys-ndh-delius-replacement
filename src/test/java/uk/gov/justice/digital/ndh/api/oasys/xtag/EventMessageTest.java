package uk.gov.justice.digital.ndh.api.oasys.xtag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import uk.gov.justice.digital.ndh.ThatsNotMyNDH;
import uk.gov.justice.digital.ndh.api.soap.SoapBody;
import uk.gov.justice.digital.ndh.api.soap.SoapEnvelopeSpec1_1;
import uk.gov.justice.digital.ndh.api.soap.SoapHeader;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMessageTest {

    @Test
    public void serializedRequestIsSchemaCompliant() throws JsonProcessingException {
        final String expected = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:even=\"http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <even:EVENT_MESSAGEInput>\n" +
                "         <!--Optional:-->\n" +
                "         <even:TIMESTAMP-VARCHAR2-IN>?</even:TIMESTAMP-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:SENTENCEYEARS-VARCHAR2-IN>?</even:SENTENCEYEARS-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:SENTENCEMONTHS-VARCHAR2-IN>?</even:SENTENCEMONTHS-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:SENTENCEDAYS-VARCHAR2-IN>?</even:SENTENCEDAYS-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:SENTENCEDATE-VARCHAR2-IN>?</even:SENTENCEDATE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:RELEASEDATE-VARCHAR2-IN>?</even:RELEASEDATE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:PRISONNUMBER-VARCHAR2-IN>?</even:PRISONNUMBER-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:PNC-VARCHAR2-IN>?</even:PNC-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:OLDPRISONNUMBER-VARCHAR2-IN>?</even:OLDPRISONNUMBER-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:NOMISID-VARCHAR2-IN>?</even:NOMISID-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:MOVEMENTFROMORTO-VARCHAR2-IN>?</even:MOVEMENTFROMORTO-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:MOVEMENTDELETE-VARCHAR2-IN>?</even:MOVEMENTDELETE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:MOVEMENTCOURTCODE-VARCHAR2-IN>?</even:MOVEMENTCOURTCODE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:MOVEMENTCODE-VARCHAR2-IN>?</even:MOVEMENTCODE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:FORENAME2-VARCHAR2-IN>?</even:FORENAME2-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:FORENAME1-VARCHAR2-IN>?</even:FORENAME1-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:FAMILYNAME-VARCHAR2-IN>?</even:FAMILYNAME-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:EVENT_TYPE-VARCHAR2-IN>?</even:EVENT_TYPE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:ESTABLISHMENTCODE-VARCHAR2-IN>?</even:ESTABLISHMENTCODE-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:EFFECTIVESENTENCELENGTH-VARCHAR2-IN>?</even:EFFECTIVESENTENCELENGTH-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:DATEOFBIRTH-VARCHAR2-IN>?</even:DATEOFBIRTH-VARCHAR2-IN>\n" +
                "         <!--Optional:-->\n" +
                "         <even:CORRELATIONID-VARCHAR2-IN>?</even:CORRELATIONID-VARCHAR2-IN>\n" +
                "      </even:EVENT_MESSAGEInput>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        final SoapEnvelopeSpec1_1 oasysRequest = SoapEnvelopeSpec1_1
                .builder()
                .header(SoapHeader.builder().build())
                .body(SoapBody
                        .builder()
                        .eventMessage(
                                EventMessage.builder()
                                        .eventType("?")
                                        .correlationId("?")
                                        .dateOfBirth("?")
                                        .effectiveSentenceLength("?")
                                        .establishmentCode("?")
                                        .familyName("?")
                                        .forename1("?")
                                        .forename2("?")
                                        .movementCode("?")
                                        .movementCourtCode("?")
                                        .movementDelete("?")
                                        .movementFromTo("?")
                                        .nomisId("?")
                                        .oldPrisonNumber("?")
                                        .pnc("?")
                                        .prisonNumber("?")
                                        .releaseDate("?")
                                        .sentenceDate("?")
                                        .sentenceDays("?")
                                        .sentenceMonths("?")
                                        .sentenceYears("?")
                                        .rawEventDateTime(LocalDateTime.MIN)
                                        .build())
                        .build())
                .build();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String actual = xmlMapper.writeValueAsString(oasysRequest);

        Diff myDiff = DiffBuilder.compare(actual).withTest(expected)
                .withDifferenceEvaluator(DifferenceEvaluators.Default)
                .ignoreComments()
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertThat(myDiff.hasDifferences()).isFalse();
    }

    @Test
    public void eventTimestampIsHandledAppropriately() throws JsonProcessingException {
        final LocalDateTime now = LocalDateTime.now();
        final String oasysTimestamp = now.format(EventMessage.DATE_TIME_FORMATTER);

        final String expectedJson = "{\"timestamp\":\"" + oasysTimestamp + "\"}";
        final String expectedXml = "<?xml version='1.0' encoding='UTF-8'?><EventMessage><wstxns1:TIMESTAMP-VARCHAR2-IN xmlns:wstxns1=\"http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE\">" + oasysTimestamp + "</wstxns1:TIMESTAMP-VARCHAR2-IN></EventMessage>";
        EventMessage eventMessage = EventMessage.builder()
                .rawEventDateTime(now)
                .build();

        assertThat(eventMessage.getRawEventDateTime()).isEqualTo(now);
        assertThat(eventMessage.getTimestamp()).isEqualTo(oasysTimestamp);

        final ThatsNotMyNDH thatsNotMyNDH = new ThatsNotMyNDH();

        ObjectMapper objectMapper = thatsNotMyNDH.objectMapper();
        assertThat(objectMapper.writeValueAsString(eventMessage)).isEqualTo(expectedJson);

        XmlMapper xmlMapper = thatsNotMyNDH.xmlMapper(thatsNotMyNDH.xmlConverter());
        assertThat(xmlMapper.writeValueAsString(eventMessage)).isEqualTo(expectedXml);
    }
}