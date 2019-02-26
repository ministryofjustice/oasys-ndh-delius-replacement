package uk.gov.justice.digital.ndh.jpa.repository.mapping;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
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
public class MappingRepositoryCsvBacked implements MappingRepository {

    private final Map<MappingCodeKey, MappingCodeData> mappingCodeData;

    @Autowired
    public MappingRepositoryCsvBacked(@Qualifier("mappingCodeDataResource") Resource csvResource) throws IOException {
        InputStream resourceStream = csvResource.getInputStream();

        Iterable<CSVRecord> records = CSVFormat.EXCEL
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(resourceStream));

        mappingCodeData = StreamSupport.stream(records.spliterator(), false)
                .map(this::mappingCodeDataOf)
                .collect(Collectors.toMap(this::keyOf, this::valueOf));
    }

    private MappingCodeData valueOf(MappingCodeData mappingCodeData) {
        return mappingCodeData;
    }

    private MappingCodeKey keyOf(MappingCodeData mappingCodeData) {
        return mappingCodeData.getKey();
    }

    @Override
    public Optional<MappingCodeData> findByCodeTypeAndSourceValue(Long codeType, String sourceValue) {
        return Optional.ofNullable(mappingCodeData.getOrDefault(
                MappingCodeKey.builder().codeType(codeType).sourceValue(sourceValue).build(), null));
    }

    private MappingCodeData mappingCodeDataOf(CSVRecord csvRecord) {
        //"CODETYPE","SOURCEVALUE","TARGETVALUE","DESCRIPTION","NUMCODE","RANK","NUMERIC1"

        return MappingCodeData.builder()
                    .codeType(Long.valueOf(csvRecord.get("CODETYPE")))
                    .sourceValue(csvRecord.get("SOURCEVALUE"))
                    .targetValue(csvRecord.get("TARGETVALUE"))
                    .description(csvRecord.get("DESCRIPTION"))
                    .numcode(getSafeLong(csvRecord, "NUMCODE"))
                    .rank(getSafeLong(csvRecord, "RANK"))
                    .numeric1(getSafeLong(csvRecord, "NUMERIC1"))
                    .build();
    }

    private Long getSafeLong(CSVRecord csvRecord, String numcode) {
        return Optional.ofNullable(csvRecord.get(numcode)).filter(Strings::isNotEmpty).map(Long::valueOf).orElse(null);
    }
}
