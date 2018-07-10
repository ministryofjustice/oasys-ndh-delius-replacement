package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

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
