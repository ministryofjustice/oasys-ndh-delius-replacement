package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Data
@Entity
@Builder
@Table(name = "TEST_MAPPING_CODE_DATA", schema = "NDH", catalog = "")
public class TestMappingCodeData {
    @Column(name = "CODETYPE")
    private Long codetype;
    @Column(name = "NOMIS_KEY")
    private String nomisKey;
    @Column(name = "LIDS_VALUE")
    private String lidsValue;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "NUMCODE")
    private Long numcode;
    @Column(name = "RANK")
    private Long rank;
    @Column(name = "NUMERIC1")
    private Long numeric1;

}
