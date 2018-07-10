package uk.gov.justice.digital.ndh.service.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;
import uk.gov.justice.digital.ndh.jpa.repository.MappingRepository;

import java.util.Optional;

@Service
public class MappingService {
    private final MappingRepository mappingRepository;

    @Autowired
    public MappingService(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public Optional<String> getTargetValueOf(Long type, String value) {

        Optional<MappingCodeData> maybeMappingCodeData = Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                .codeType(type)
                .sourceValue(value)
                .build()));
        return maybeMappingCodeData.map(MappingCodeData::getTargetValue);
    }
    public Optional<String> getDescriptionOf(Long type, String value) {

        Optional<MappingCodeData> maybeMappingCodeData = Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                .codeType(type)
                .sourceValue(value)
                .build()));
        return maybeMappingCodeData.map(MappingCodeData::getDescription);
    }
    public Optional<Long> getNumCodeOf(Long type, String value) {

        Optional<MappingCodeData> maybeMappingCodeData = Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                .codeType(type)
                .sourceValue(value)
                .build()));
        return maybeMappingCodeData.map(MappingCodeData::getNumcode);
    }
    public Optional<Long> getRankOf(Long type, String value) {

        Optional<MappingCodeData> maybeMappingCodeData = Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                .codeType(type)
                .sourceValue(value)
                .build()));
        return maybeMappingCodeData.map(MappingCodeData::getRank);
    }
    public Optional<Long> getNumeric1Of(Long type, String value) {

        Optional<MappingCodeData> maybeMappingCodeData = Optional.ofNullable(mappingRepository.findOne(MappingCodeDataPK.builder()
                .codeType(type)
                .sourceValue(value)
                .build()));
        return maybeMappingCodeData.map(MappingCodeData::getNumeric1);
    }
}
