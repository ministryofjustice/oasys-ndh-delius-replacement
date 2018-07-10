package uk.gov.justice.digital.ndh.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.ndh.api.Mapping;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeData;
import uk.gov.justice.digital.ndh.jpa.entity.MappingCodeDataPK;

@Repository
public interface MappingRepository extends JpaRepository<MappingCodeData, MappingCodeDataPK> {
}
