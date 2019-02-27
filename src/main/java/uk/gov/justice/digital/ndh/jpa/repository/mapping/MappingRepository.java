package uk.gov.justice.digital.ndh.jpa.repository.mapping;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MappingRepository {
    Optional<MappingCodeData> findByCodeTypeAndSourceValue(Long codeType, String sourceValue);
}
