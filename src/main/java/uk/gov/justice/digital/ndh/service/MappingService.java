package uk.gov.justice.digital.ndh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;
import uk.gov.justice.digital.ndh.jpa.repository.MappingRepository;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;

import java.util.Optional;

@Service
@Slf4j
public class MappingService {
    private final MappingRepository mappingRepository;

    @Autowired
    public MappingService(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public String descriptionOf(String sourceVal, Long codeType) throws NDHMappingException {
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        if (!maybeMapped.isPresent()) {
            log.error("Could not map source {} and code {} to description.", sourceVal, codeType);
        }

        return maybeMapped.map(MappingCodeData::getDescription).orElseThrow(() -> NDHMappingException.builder()
                .code(codeType)
                .sourceValue(sourceVal)
                .subject("description")
                .build());
    }

    public String targetValueOf(String sourceVal, Long codeType) throws NDHMappingException {
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        if (!maybeMapped.isPresent()) {
            log.error("Could not map source {} and code {} to targetValue.", sourceVal, codeType);
        }

        return maybeMapped.map(MappingCodeData::getTargetValue).orElseThrow(() ->
                NDHMappingException.builder()
                        .sourceValue(sourceVal)
                        .code(codeType)
                        .subject("targetValue")
                        .build());

    }

}
