package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Sets;
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
    private final Map<String,String> ignoredRequirementCodes;

    @Autowired
    public RequirementLookupRepositoryCsvBacked(@Qualifier("requirementLookupResource") Resource csvResource,
                                                @Qualifier("requirementBlacklistResource") Resource requirementBlacklistResource) throws IOException {
        InputStream resourceStream = csvResource.getInputStream();

        Iterable<CSVRecord> records = CSVFormat.EXCEL
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(resourceStream));

        requirementLookupMap = StreamSupport.stream(records.spliterator(), false)
                .map(this::requirementLookupOf)
                .collect(Collectors.toMap(this::keyOf, this::valueOf));


        ignoredRequirementCodes = (Map<String,String>)new ObjectMapper().readValue(requirementBlacklistResource.getInputStream(), Map.class);

        var whitelistCodes = requirementLookupMap.keySet()
                .stream()
                .filter(rlk -> "N".equals(rlk.getReqType()))
                .map(RequirementLookupKey::getReqCode)
                .collect(Collectors.toSet());
        
        var blacklistCodes = ignoredRequirementCodes.keySet();

        var intersection = Sets.intersection(whitelistCodes, blacklistCodes);

        if (!intersection.isEmpty()) {
            log.error("Requirements mapping contains blacklisted (unwanted) requirement codes!: {}", intersection.toString());
        }
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

        if (ignoredRequirementCodes.containsKey(reqCode)) {
            log.info("Ignoring blacklisted requirement: {}", reqCode);
            return Optional.empty();
        }

        var maybeMapped = Optional.ofNullable(requirementLookupMap.getOrDefault(
                RequirementLookupKey.builder()
                        .reqCode(reqCode)
                        .reqType(reqType)
                        .subCode(subCode)
                        .build(), null));

        if (maybeMapped.isPresent()) {
            return maybeMapped;
        }

        log.warn("Failed to resolve primary mapping reqType {}, reqCode {}, subCode {}. Falling back to resolve by reqCode alone...", reqType, reqCode, subCode);

        var maybeMapped2 = Optional.ofNullable(requirementLookupMap.getOrDefault(
                RequirementLookupKey.builder()
                        .reqCode(reqCode)
                        .reqType(reqType)
                        .subCode("")
                        .build(), null));

        if (maybeMapped2.isPresent()) {
            return maybeMapped2;
        }

        log.error("Failed to resolve fallback mapping reqType {}, reqCode {}, subCode \"\".", reqType, reqCode);
        return Optional.empty();

    }
}
