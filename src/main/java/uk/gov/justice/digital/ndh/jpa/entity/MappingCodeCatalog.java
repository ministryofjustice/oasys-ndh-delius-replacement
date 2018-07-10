package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Data
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
