package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class MsgStorePK implements Serializable {
    @Column(name = "MSG_STORE_SEQ")
    @Id
    private Integer msgStoreSeq;
    @Column(name = "STORE_DATETIME")
    @Id
    private Timestamp storeDatetime;

}
