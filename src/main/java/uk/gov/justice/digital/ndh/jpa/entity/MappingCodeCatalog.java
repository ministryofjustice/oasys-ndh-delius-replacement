package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.Builder;
import lombok.Value;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Value
@Builder
@Entity
@Table(name = "MAPPING_CODE_CATALOG")
public class MappingCodeCatalog {
    @Id
    @Column(name = "CODETYPE")
    private Long codetype;
    @Column(name = "DESCRIPTION")
    private String description;

}
