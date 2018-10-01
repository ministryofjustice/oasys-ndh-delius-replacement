package uk.gov.justice.digital.ndh.api.nomis;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class Phone {
    private Long phoneId;
    private String ownerClass;
    @JsonIgnore
    private IdPair relationship;
    @JsonAnyGetter
    private Map<String, Object> serializeRelationship() {
        return relationship.asMap();
    }
    private Integer ownerSeq;
    private String ownerCode;
    private String phoneType;
    private String phoneNo;
    private String extNo;
}
