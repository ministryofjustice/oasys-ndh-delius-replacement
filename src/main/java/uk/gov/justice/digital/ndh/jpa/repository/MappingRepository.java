package uk.gov.justice.digital.ndh.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;

import java.util.Optional;

@Repository
public interface MappingRepository extends JpaRepository<MappingCodeData, MappingCodeDataPK> {
    Optional<MappingCodeData> findByTargetValueAndCodeType(String targetVal, long codeType);
}
