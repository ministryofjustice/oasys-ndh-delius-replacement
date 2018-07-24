package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "EXCEPTION_LOG")
@IdClass(ExceptionLogPK.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionLog {
    @Id
    @Column(name = "EXC_SEQ")
    private Integer excSeq;
    @Id
    @Column(name = "EXC_DATETIME")
    private Timestamp excDatetime;
    @Column(name = "EXC_CODE")
    private String excCode;
    @Column(name = "CORRELATION_ID")
    private String correlationId;
    @Column(name = "CUSTOM_ID")
    private String customId;
    @Column(name = "PROCESS_ID")
    private Integer processId;
    @Column(name = "PROCESS_NAME")
    private String processName;
    @Column(name = "BWPROCESS_NAME")
    private String bwprocessName;
    @Column(name = "SEVERITY")
    private Integer severity;
    @Column(name = "CATEGORY")
    private String category;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "BUSINESS_ERROR_INFO")
    private String businessErrorInfo;
    @Column(name = "ERR_STACKTRACE")
    private String errStacktrace;
    @Column(name = "ERR_MSG")
    private String errMsg;
    @Column(name = "ERR_FULLCLASS")
    private String errFullclass;
    @Column(name = "ERR_CLASS")
    private String errClass;
    @Column(name = "PAYLOAD_ZIP_FLAG")
    private String payloadZipFlag;
    @Column(name = "PAYLOAD_CONTENT_TYPE")
    private String payloadContentType;
    @Column(name = "PAYLOAD", length = 10000)
    private String payload;

}
