package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingCodeDataPK implements Serializable {
    @Column(name = "CODETYPE")
    @Id
    private Long codeType;
    @Column(name = "SOURCEVALUE")
    @Id
    private String sourceValue;

}
