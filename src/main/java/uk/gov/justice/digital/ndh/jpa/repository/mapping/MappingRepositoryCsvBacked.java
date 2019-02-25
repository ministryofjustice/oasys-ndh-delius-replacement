package uk.gov.justice.digital.ndh.jpa.repository.mapping;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MappingRepositoryCsvBacked implements MappingRepository {

    private final Map<MappingCodeKey, MappingCodeData> mappingCodeData;

    @Autowired
    public MappingRepositoryCsvBacked(@Qualifier("mappingCodeDataResource") Resource csvResource) throws IOException {
        InputStream resource = csvResource.getInputStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource))) {
            mappingCodeData = reader.lines()
                    .skip(1)
                    .map(this::mappingCodeDataOf)
                    .collect(Collectors.toMap(this::keyOf, this::valueOf));
        }
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

    private MappingCodeData mappingCodeDataOf(String s) {
        //"CODETYPE","SOURCEVALUE","TARGETVALUE","DESCRIPTION","NUMCODE","RANK","NUMERIC1"

        var fields = s.split(",");

        return MappingCodeData.builder()
                .codeType(Long.valueOf(fields[0]))
                .sourceValue(fields[1])
                .targetValue(fields[2])
                .description(fields[3])
                .numcode(Long.valueOf(fields[4]))
                .rank(Long.valueOf(fields[5]))
                .numeric1(Long.valueOf(fields[6]))
                .build();
    }
}
