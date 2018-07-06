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
@Table(name = "TEST_MAPPING_CODE_CATALOG", schema = "NDH", catalog = "")
public class TestMappingCodeCatalog {
    @Column(name = "CODETYPE")
    private Long codetype;
    @Column(name = "DESCRIPTION")
    private String description;


}
