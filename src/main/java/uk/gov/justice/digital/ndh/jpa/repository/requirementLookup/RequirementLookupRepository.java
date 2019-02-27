package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;

import java.util.Optional;

public interface RequirementLookupRepository {
    Optional<RequirementLookup> findByReqTypeAndReqCodeAndSubCode(String reqType, String reqCode, String subCode);
}
