package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class RequirementLookupRepositoryCsvBacked implements RequirementLookupRepository {

    private final Map<RequirementLookupKey, RequirementLookup> requirementLookupMap;

    @Autowired
    public RequirementLookupRepositoryCsvBacked(@Qualifier("requirementLookupResource") Resource csvResource) throws IOException {
        InputStream resourceStream = csvResource.getInputStream();

        Iterable<CSVRecord> records = CSVFormat.EXCEL
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(resourceStream));

        requirementLookupMap = StreamSupport.stream(records.spliterator(), false)
                .map(this::requirementLookupOf)
                .collect(Collectors.toMap(this::keyOf, this::valueOf));
    }

    private RequirementLookup requirementLookupOf(CSVRecord csvRecord) {
        //"REQ_TYPE","REQ_CODE","SUB_CODE","SENTENCE_ATTRIBUTE_CAT","SENTENCE_ATTRIBUTE_ELM","CJA_UNPAID_HOURS","CJA_SUPERVISION_MONTHS","ACTIVITY_DESC"

        return RequirementLookup.builder()
                .reqType(csvRecord.get("REQ_TYPE"))
                .reqCode(csvRecord.get("REQ_CODE"))
                .subCode(csvRecord.get("SUB_CODE"))
                .sentenceAttributeCat(csvRecord.get("SENTENCE_ATTRIBUTE_CAT"))
                .sentenceAttributeElm(csvRecord.get("SENTENCE_ATTRIBUTE_ELM"))
                .cjaUnpaidHours(csvRecord.get("CJA_UNPAID_HOURS"))
                .cjaSupervisionMonths(csvRecord.get("CJA_SUPERVISION_MONTHS"))
                .activityDesc(csvRecord.get("ACTIVITY_DESC"))
                .build();
    }

    private RequirementLookup valueOf(RequirementLookup requirementLookup) {
        return requirementLookup;
    }

    private RequirementLookupKey keyOf(RequirementLookup requirementLookup) {
        return requirementLookup.getKey();
    }

    @Override
    public Optional<RequirementLookup> findByReqTypeAndReqCodeAndSubCode(String reqType, String reqCode, String subCode) {
        return Optional.ofNullable(requirementLookupMap.getOrDefault(
                RequirementLookupKey.builder()
                        .reqCode(reqCode)
                        .reqType(reqType)
                        .subCode(subCode)
                        .build(), null));
    }
}