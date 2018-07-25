package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Builder
@Entity
@Table(name = "MAPPING_CODE_DATA")
@IdClass(MappingCodeDataPK.class)
public class MappingCodeData {
    @Id
    @Column(name = "CODETYPE")
    private Long codeType;
    @Id
    @Column(name = "SOURCEVALUE")
    private String sourceValue;
    @Column(name = "TARGETVALUE")
    private String targetValue;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "NUMCODE")
    private Long numcode;
    @Column(name = "RANK")
    private Long rank;
    @Column(name = "NUMERIC1")
    private Long numeric1;

}
