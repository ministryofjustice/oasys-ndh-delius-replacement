package uk.gov.justice.digital.ndh.service.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;
import uk.gov.justice.digital.ndh.jpa.repository.MappingRepository;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;

import java.util.Optional;

@Service
public class MappingService {
    private final MappingRepository mappingRepository;

    private final ExceptionLogService exceptionLogService;

    @Autowired
    public MappingService(MappingRepository mappingRepository, ExceptionLogService exceptionLogService) {
        this.mappingRepository = mappingRepository;
        this.exceptionLogService = exceptionLogService;
    }

    public String descriptionOf(String sourceVal, Long codeType) {
        //TODO record any mapping failures
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        return maybeMapped.map(MappingCodeData::getDescription).orElse(fail(codeType,sourceVal));
    }

    private String fail(Long codeType, String sourceVal) {
        exceptionLogService.logMappingFail(codeType, sourceVal);
        return null;
    }

    public String targetValueOf(String sourceVal, Long codeType) {
        //TODO record any mapping failures
        Optional<MappingCodeData> maybeMapped = Optional.ofNullable(sourceVal).flatMap(
                sv -> Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                        .codeType(codeType)
                        .sourceValue(sourceVal)
                        .build())));

        return maybeMapped.map(MappingCodeData::getTargetValue).orElse(fail(codeType,sourceVal));

    }

}
