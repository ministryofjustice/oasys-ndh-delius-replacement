package uk.gov.justice.digital.ndh.jpa.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "MSG_STORE", schema = "NDH", catalog = "")
@IdClass(MsgStorePK.class)
public class MsgStore {
    private Integer msgStoreSeq;
    private String correlationId;
    private String customId;
    private Timestamp msgTimestamp;
    private Timestamp storeDatetime;
    private String processName;
    private String msgProcState;
    private String msgBadFlag;
    private String msgRecoverFlag;
    private String msgDeliveredFlag;
    private String payloadZipFlag;
    private String payloadContentType;
    private String payload;

    @Id
    @Column(name = "MSG_STORE_SEQ")
    public Integer getMsgStoreSeq() {
        return msgStoreSeq;
    }

    public void setMsgStoreSeq(Integer msgStoreSeq) {
        this.msgStoreSeq = msgStoreSeq;
    }

    @Basic
    @Column(name = "CORRELATION_ID")
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Basic
    @Column(name = "CUSTOM_ID")
    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    @Basic
    @Column(name = "MSG_TIMESTAMP")
    public Timestamp getMsgTimestamp() {
        return msgTimestamp;
    }

    public void setMsgTimestamp(Timestamp msgTimestamp) {
        this.msgTimestamp = msgTimestamp;
    }

    @Id
    @Column(name = "STORE_DATETIME")
    public Timestamp getStoreDatetime() {
        return storeDatetime;
    }

    public void setStoreDatetime(Timestamp storeDatetime) {
        this.storeDatetime = storeDatetime;
    }

    @Basic
    @Column(name = "PROCESS_NAME")
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Basic
    @Column(name = "MSG_PROC_STATE")
    public String getMsgProcState() {
        return msgProcState;
    }

    public void setMsgProcState(String msgProcState) {
        this.msgProcState = msgProcState;
    }

    @Basic
    @Column(name = "MSG_BAD_FLAG")
    public String getMsgBadFlag() {
        return msgBadFlag;
    }

    public void setMsgBadFlag(String msgBadFlag) {
        this.msgBadFlag = msgBadFlag;
    }

    @Basic
    @Column(name = "MSG_RECOVER_FLAG")
    public String getMsgRecoverFlag() {
        return msgRecoverFlag;
    }

    public void setMsgRecoverFlag(String msgRecoverFlag) {
        this.msgRecoverFlag = msgRecoverFlag;
    }

    @Basic
    @Column(name = "MSG_DELIVERED_FLAG")
    public String getMsgDeliveredFlag() {
        return msgDeliveredFlag;
    }

    public void setMsgDeliveredFlag(String msgDeliveredFlag) {
        this.msgDeliveredFlag = msgDeliveredFlag;
    }

    @Basic
    @Column(name = "PAYLOAD_ZIP_FLAG")
    public String getPayloadZipFlag() {
        return payloadZipFlag;
    }

    public void setPayloadZipFlag(String payloadZipFlag) {
        this.payloadZipFlag = payloadZipFlag;
    }

    @Basic
    @Column(name = "PAYLOAD_CONTENT_TYPE")
    public String getPayloadContentType() {
        return payloadContentType;
    }

    public void setPayloadContentType(String payloadContentType) {
        this.payloadContentType = payloadContentType;
    }

    @Basic
    @Column(name = "PAYLOAD")
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgStore msgStore = (MsgStore) o;
        return Objects.equals(msgStoreSeq, msgStore.msgStoreSeq) &&
                Objects.equals(correlationId, msgStore.correlationId) &&
                Objects.equals(customId, msgStore.customId) &&
                Objects.equals(msgTimestamp, msgStore.msgTimestamp) &&
                Objects.equals(storeDatetime, msgStore.storeDatetime) &&
                Objects.equals(processName, msgStore.processName) &&
                Objects.equals(msgProcState, msgStore.msgProcState) &&
                Objects.equals(msgBadFlag, msgStore.msgBadFlag) &&
                Objects.equals(msgRecoverFlag, msgStore.msgRecoverFlag) &&
                Objects.equals(msgDeliveredFlag, msgStore.msgDeliveredFlag) &&
                Objects.equals(payloadZipFlag, msgStore.payloadZipFlag) &&
                Objects.equals(payloadContentType, msgStore.payloadContentType) &&
                Objects.equals(payload, msgStore.payload);
    }

    @Override
    public int hashCode() {

        return Objects.hash(msgStoreSeq, correlationId, customId, msgTimestamp, storeDatetime, processName, msgProcState, msgBadFlag, msgRecoverFlag, msgDeliveredFlag, payloadZipFlag, payloadContentType, payload);
    }
}
