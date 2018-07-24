package uk.gov.justice.digital.ndh.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.ndh.jpa.entity.ExceptionLog;
import uk.gov.justice.digital.ndh.jpa.entity.ExceptionLogPK;

@Repository
public interface ExceptionLogRepository extends JpaRepository<ExceptionLog, ExceptionLogPK> {
}
