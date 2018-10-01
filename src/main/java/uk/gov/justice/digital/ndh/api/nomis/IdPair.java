package uk.gov.justice.digital.ndh.api.nomis;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class IdPair {
    private String key;
    private Object value;

    public Map<String, Object> asMap() {
        return ImmutableMap.of(key, value);
    }
}
