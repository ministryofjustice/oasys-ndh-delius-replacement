package uk.gov.justice.digital.ndh.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class RequirementLookupRepositoryDataSourceBacked implements RequirementLookupRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RequirementLookupRepositoryDataSourceBacked(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RequirementLookup> findByReqTypeAndReqCodeAndSubCode(String reqType, String reqCode, String subCode) {

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM REQUIREMENT_LOOKUP R WHERE REQ_TYPE = ? AND REQ_CODE = ? and SUB_CODE = ?",
                    (rs, rowNum) -> RequirementLookup.builder()
                            .activityDesc(rs.getString("ACTIVITY_DESC"))
                            .cjaSupervisionMonths(rs.getString("CJA_SUPERVISION_MONTHS"))
                            .cjaUnpaidHours(rs.getString("CJA_UNPAID_HOURS"))
                            .reqCode(rs.getString("REQ_CODE"))
                            .reqType(rs.getString("REQ_TYPE"))
                            .sentenceAttributeCat(rs.getString("SENTENCE_ATTRIBUTE_CAT"))
                            .sentenceAttributeElm(rs.getString("SENTENCE_ATTRIBUTE_ELM"))
                            .subCode(rs.getString("SUB_CODE"))
                            .build(),
                    reqType, reqCode, subCode));
        } catch (DataAccessException dae) {
            log.error(dae.getMessage());
            return Optional.empty();
        }
    }
}
