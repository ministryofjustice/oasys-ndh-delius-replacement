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
    @SequenceGenerator(name = "EXC_SEQ", sequenceName = "EXC_SEQ")
    @GeneratedValue(generator = "EXC_SEQ", strategy = GenerationType.SEQUENCE)
    @Id
    private Integer excSeq;
    @Column(name = "EXC_DATETIME")
    @Id
    private Timestamp excDatetime;

}
