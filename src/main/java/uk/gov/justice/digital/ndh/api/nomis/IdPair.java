package uk.gov.justice.digital.ndh.api.nomis;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class IdPair {
    private String key;
    private Object value;

    public Map<String, Object> asMap() {
        return ImmutableMap.of(key, value);
    }
}
