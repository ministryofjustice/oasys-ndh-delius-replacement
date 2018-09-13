package uk.gov.justice.digital.ndh.api.oasys.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OffenderDetail {
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "CMSProbNumber")
    private String cmsProbNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "PrisonNumber")
    private String prisonNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "NomisID")
    private String nomisId;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "FamilyName")
    private String familyName;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Forename1")
    private String forename1;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Forename2")
    private String forename2;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Forename3")
    private String forename3;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Gender")
    private String gender;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DateOfBirth")
    private String dateOfBirth;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Alias")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Alias> aliases;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "EthnicCategory")
    private String ethnicCategory;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine1")
    private String addressLine1;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine2")
    private String addressLine2;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine3")
    private String addressLine3;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine4")
    private String addressLine4;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine5")
    private String addressLine5;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AddressLine6")
    private String addressLine6;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "PostCode")
    private String postCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "TelephoneNumber")
    private String telephoneNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "PNC")
    private String pnc;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "CRONumber")
    private String croNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Language")
    private String language;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "Religion")
    private String religion;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "ReleaseDate")
    private String releaseDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "ReleaseType")
    private String releaseType;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "License")
    private String license;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "LAOIndicator")
    private String laoIndicator;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "CellLocation")
    private String cellLocation;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "SecurityCategory")
    private String securityCategory;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine1")
    private String dischargeAddressLine1;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine2")
    private String dischargeAddressLine2;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine3")
    private String dischargeAddressLine3;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine4")
    private String dischargeAddressLine4;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine5")
    private String dischargeAddressLine5;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeAddressLine6")
    private String dischargeAddressLine6;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargePostcode")
    private String dischargePostCode;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "DischargeTelphoneNumber")
    private String dischargeTelephoneNumber;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AppealPendingIndicator")
    private String appealPendingIndicator;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "CurfewDate")
    private String curfewDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "ParoleEligibilityDate")
    private String paroleEligibilityDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "LicenceExpiryDate")
    private String licenceExpiryDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "SentenceExpiryDate")
    private String sentenceExpiryDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "NonParoleDate")
    private String nonParoleDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "AutomaticReleaseDate")
    private String automaticReleaseDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "ConditionalReleaseDate")
    private String conditionalReleaseDate;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "RiskOfSelfHarm")
    private String riskOfSelfHarm;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "NumberOfOffences")
    private String numberOfOffences;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "FirstConvictionAge")
    private String firstConvictionAge;
    @JacksonXmlProperty(namespace = "http://www.hp.com/NDH_Web_Service/offender", localName = "NOMCondition")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> nomConditions;


}
