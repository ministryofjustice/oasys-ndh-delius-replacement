package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "MSG_STORE")
@IdClass(MsgStorePK.class)
public class MsgStore {
    @Id
    @Column(name = "MSG_STORE_SEQ")
    private Integer msgStoreSeq;
    @Id
    @Column(name = "STORE_DATETIME")
    private Timestamp storeDatetime;
    @Column(name = "CORRELATION_ID")
    private String correlationId;
    @Column(name = "CUSTOM_ID")
    private String customId;
    @Column(name = "MSG_TIMESTAMP")
    private Timestamp msgTimestamp;
    @Column(name = "PROCESS_NAME")
    private String processName;
    @Column(name = "MSG_PROC_STATE")
    private String msgProcState;
    @Column(name = "MSG_BAD_FLAG")
    private String msgBadFlag;
    @Column(name = "MSG_RECOVER_FLAG")
    private String msgRecoverFlag;
    @Column(name = "MSG_DELIVERED_FLAG")
    private String msgDeliveredFlag;
    @Column(name = "PAYLOAD_ZIP_FLAG")
    private String payloadZipFlag;
    @Column(name = "PAYLOAD_CONTENT_TYPE")
    private String payloadContentType;
    @Column(name = "PAYLOAD")
    private String payload;

}
