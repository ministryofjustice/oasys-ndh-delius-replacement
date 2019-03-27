package uk.gov.justice.digital.ndh.api.oasys.xtag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
@Builder
public class EventMessage {
    @JsonIgnore
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSS");

    @JsonIgnore
    private LocalDateTime rawEventDateTime;

    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "TIMESTAMP-VARCHAR2-IN")
    public String getTimestamp() {
        return rawEventDateTime.format(DATE_TIME_FORMATTER);
    }

    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "SENTENCEYEARS-VARCHAR2-IN")
    private String sentenceYears;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "SENTENCEMONTHS-VARCHAR2-IN")
    private String sentenceMonths;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "SENTENCEDAYS-VARCHAR2-IN")
    private String sentenceDays;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "SENTENCEDATE-VARCHAR2-IN")
    private String sentenceDate;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "RELEASEDATE-VARCHAR2-IN")
    private String releaseDate;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "PRISONNUMBER-VARCHAR2-IN")
    private String prisonNumber;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "PNC-VARCHAR2-IN")
    private String pnc;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "OLDPRISONNUMBER-VARCHAR2-IN")
    private String oldPrisonNumber;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "NOMISID-VARCHAR2-IN")
    private String nomisId;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "MOVEMENTFROMORTO-VARCHAR2-IN")
    private String movementFromTo;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "MOVEMENTDELETE-VARCHAR2-IN")
    private String movementDelete;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "MOVEMENTCOURTCODE-VARCHAR2-IN")
    private String movementCourtCode;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "MOVEMENTCODE-VARCHAR2-IN")
    private String movementCode;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "FORENAME2-VARCHAR2-IN")
    private String forename2;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "FORENAME1-VARCHAR2-IN")
    private String forename1;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "FAMILYNAME-VARCHAR2-IN")
    private String familyName;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "EVENT_TYPE-VARCHAR2-IN")
    private String eventType;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "ESTABLISHMENTCODE-VARCHAR2-IN")
    private String establishmentCode;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "EFFECTIVESENTENCELENGTH-VARCHAR2-IN")
    private String effectiveSentenceLength;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "DATEOFBIRTH-VARCHAR2-IN")
    private String dateOfBirth;
    @JacksonXmlProperty(namespace = "http://xmlns.oracle.com/orawsv/EOR/SERVICES_PKG/EVENT_MESSAGE", localName = "CORRELATIONID-VARCHAR2-IN")
    private String correlationId;

}