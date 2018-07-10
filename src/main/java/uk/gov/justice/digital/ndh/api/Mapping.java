package uk.gov.justice.digital.ndh.api;


import lombok.*;

@Data
@Builder
public class Mapping {
    private Long codeType;
    private String sourceValue;
    private String targetValue;
    private String description;
    private Long numCode;
    private Long rank;
    private Long numeric1;
}
