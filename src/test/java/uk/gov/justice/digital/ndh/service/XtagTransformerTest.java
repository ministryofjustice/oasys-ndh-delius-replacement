package uk.gov.justice.digital.ndh.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class XtagTransformerTest {

    @Test
    public void pncIsNormalised() {
        assertThat(XtagTransformer.normalisedPncOf("1978/0111942M")).isEqualTo("78/0111942M");
        assertThat(XtagTransformer.normalisedPncOf("78/0111942M")).isEqualTo("78/0111942M");

    }
}