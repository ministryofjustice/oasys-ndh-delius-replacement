package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ExceptionLogPK implements Serializable {
    @Column(name = "EXC_SEQ")
    @SequenceGenerator(name = "EXCEPTION_LOG_SEQUENCE_GENERATOR", sequenceName = "EXCEPTION_LOG_SEQ")
    @GeneratedValue(generator = "EXCEPTION_LOG_SEQUENCE_GENERATOR", strategy = GenerationType.SEQUENCE)
    @Id
    private Integer excSeq;
    @Column(name = "EXC_DATETIME")
    @Id
    private Timestamp excDatetime;

}
