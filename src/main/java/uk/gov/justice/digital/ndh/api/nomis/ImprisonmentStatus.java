package uk.gov.justice.digital.ndh.api.nomis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImprisonmentStatus {
    private Long imprisonmentStatusId;
    private String imprisonmentStatus;
    private String description;
    private String bandCode;
    private Integer rankValue;
    private Integer imprisonmentStatusSeq;
}
