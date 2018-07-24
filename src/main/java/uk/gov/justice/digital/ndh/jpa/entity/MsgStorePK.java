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
public class MsgStorePK implements Serializable {
    @Column(name = "MSG_STORE_SEQ")
    @SequenceGenerator(name = "MSG_STORE_SEQUENCE_GENERATOR", sequenceName = "MSG_STORE_SEQ")
    @GeneratedValue(generator = "MSG_STORE_SEQUENCE_GENERATOR", strategy = GenerationType.SEQUENCE)
    @Id
    private Integer msgStoreSeq;
    @Column(name = "STORE_DATETIME")
    @Id
    private Timestamp storeDatetime;

}
