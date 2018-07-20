package uk.gov.justice.digital.ndh.jpa.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "EXCEPTION_LOG", schema = "NDH", catalog = "")
@IdClass(ExceptionLogPK.class)
public class ExceptionLog {
    private Integer excSeq;
    private String excCode;
    private String correlationId;
    private String customId;
    private Integer processId;
    private String processName;
    private String bwprocessName;
    private Integer severity;
    private String category;
    private Timestamp excDatetime;
    private String description;
    private String businessErrorInfo;
    private String errStacktrace;
    private String errMsg;
    private String errFullclass;
    private String errClass;
    private String payloadZipFlag;
    private String payloadContentType;
    private String payload;

    @Id
    @Column(name = "EXC_SEQ")
    public Integer getExcSeq() {
        return excSeq;
    }

    public void setExcSeq(Integer excSeq) {
        this.excSeq = excSeq;
    }

    @Basic
    @Column(name = "EXC_CODE")
    public String getExcCode() {
        return excCode;
    }

    public void setExcCode(String excCode) {
        this.excCode = excCode;
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
    @Column(name = "PROCESS_ID")
    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
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
    @Column(name = "BWPROCESS_NAME")
    public String getBwprocessName() {
        return bwprocessName;
    }

    public void setBwprocessName(String bwprocessName) {
        this.bwprocessName = bwprocessName;
    }

    @Basic
    @Column(name = "SEVERITY")
    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    @Basic
    @Column(name = "CATEGORY")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Id
    @Column(name = "EXC_DATETIME")
    public Timestamp getExcDatetime() {
        return excDatetime;
    }

    public void setExcDatetime(Timestamp excDatetime) {
        this.excDatetime = excDatetime;
    }

    @Basic
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "BUSINESS_ERROR_INFO")
    public String getBusinessErrorInfo() {
        return businessErrorInfo;
    }

    public void setBusinessErrorInfo(String businessErrorInfo) {
        this.businessErrorInfo = businessErrorInfo;
    }

    @Basic
    @Column(name = "ERR_STACKTRACE")
    public String getErrStacktrace() {
        return errStacktrace;
    }

    public void setErrStacktrace(String errStacktrace) {
        this.errStacktrace = errStacktrace;
    }

    @Basic
    @Column(name = "ERR_MSG")
    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Basic
    @Column(name = "ERR_FULLCLASS")
    public String getErrFullclass() {
        return errFullclass;
    }

    public void setErrFullclass(String errFullclass) {
        this.errFullclass = errFullclass;
    }

    @Basic
    @Column(name = "ERR_CLASS")
    public String getErrClass() {
        return errClass;
    }

    public void setErrClass(String errClass) {
        this.errClass = errClass;
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
        ExceptionLog that = (ExceptionLog) o;
        return Objects.equals(excSeq, that.excSeq) &&
                Objects.equals(excCode, that.excCode) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(customId, that.customId) &&
                Objects.equals(processId, that.processId) &&
                Objects.equals(processName, that.processName) &&
                Objects.equals(bwprocessName, that.bwprocessName) &&
                Objects.equals(severity, that.severity) &&
                Objects.equals(category, that.category) &&
                Objects.equals(excDatetime, that.excDatetime) &&
                Objects.equals(description, that.description) &&
                Objects.equals(businessErrorInfo, that.businessErrorInfo) &&
                Objects.equals(errStacktrace, that.errStacktrace) &&
                Objects.equals(errMsg, that.errMsg) &&
                Objects.equals(errFullclass, that.errFullclass) &&
                Objects.equals(errClass, that.errClass) &&
                Objects.equals(payloadZipFlag, that.payloadZipFlag) &&
                Objects.equals(payloadContentType, that.payloadContentType) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {

        return Objects.hash(excSeq, excCode, correlationId, customId, processId, processName, bwprocessName, severity, category, excDatetime, description, businessErrorInfo, errStacktrace, errMsg, errFullclass, errClass, payloadZipFlag, payloadContentType, payload);
    }
}
