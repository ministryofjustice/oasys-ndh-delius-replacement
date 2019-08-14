package uk.gov.justice.digital.ndh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.justice.digital.ndh.jpa.repository.mapping.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.repository.mapping.MappingRepository;
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
        if(StringUtils.isEmpty(sourceVal)) {
            return null;
        }

        Optional<MappingCodeData> maybeMapped = getMaybeMapped(sourceVal, codeType);

        if (maybeMapped.isEmpty()) {
            log.error("Could not map source {} and code {} to description.", sourceVal, codeType);
        }

        return maybeMapped.map(MappingCodeData::getDescription).orElseThrow(() -> NDHMappingException.builder()
                .code(codeType)
                .value(sourceVal)
                .subject("description")
                .build());
    }

    public String targetValueOf(String sourceVal, Long codeType) throws NDHMappingException {
        return targetValueOf(sourceVal, codeType, true);
    }

    public String targetValueOf(String sourceVal, Long codeType, boolean logMapFail) throws NDHMappingException {
        if(StringUtils.isEmpty(sourceVal)) {
            return null;
        }

        Optional<MappingCodeData> maybeMapped = getMaybeMapped(sourceVal, codeType);

        if (maybeMapped.isEmpty() && logMapFail) {
            log.error("Could not map source {} and code {} to targetValue.", sourceVal, codeType);
        }

        return maybeMapped.map(MappingCodeData::getTargetValue).orElseThrow(() ->
                NDHMappingException.builder()
                        .value(sourceVal)
                        .code(codeType)
                        .subject("targetValue")
                        .build());

    }

    public Optional<MappingCodeData> getMaybeMapped(String sourceVal, Long codeType) {
        return mappingRepository.findByCodeTypeAndSourceValue(codeType, sourceVal);
    }

    public Long numeric1Of(String sourceVal, Long codeType) throws NDHMappingException {
        Optional<MappingCodeData> maybeMapped = getMaybeMapped(sourceVal, codeType);

        if (maybeMapped.isEmpty()) {
            log.error("Could not map source {} and code {} to numeric1.", sourceVal, codeType);
        }

        return maybeMapped.map(MappingCodeData::getNumeric1).orElseThrow(() ->
                NDHMappingException.builder()
                        .value(sourceVal)
                        .code(codeType)
                        .subject("numeric1")
                        .build());
    }
}
