package uk.gov.justice.digital.ndh.api.delius.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Category {
    @JsonProperty("Code")
    private String code;
    @JsonProperty("MainCategory")
    private String mainCategory;
    @JsonProperty("SubCategory")
    private String subCategory;
    @JsonProperty("RequirementDetails")
    private RequirementDetails requirementDetails;

}
