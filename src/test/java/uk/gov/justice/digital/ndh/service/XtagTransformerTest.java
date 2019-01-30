package uk.gov.justice.digital.ndh.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderAlias;

import static org.assertj.core.api.Assertions.assertThat;


public class XtagTransformerTest {

    @Test
    public void pncIsNormalised() {
        assertThat(XtagTransformer.normalisedPncOf("1978/0111942M")).isEqualTo("78/0111942M");
        assertThat(XtagTransformer.normalisedPncOf("78/0111942M")).isEqualTo("78/0111942M");
    }

    @Test
    public void pncOfOffenderIsDerivedFromAllIdentifiers() {
        Offender anOffenderWithIdentifiersAtRootLevel = Offender.builder()
                .identifiers(ImmutableList.of(Identifier.builder()
                        .identifierType("PNC")
                        .identifier("70/1122A")
                        .build()))
                .build();

        Offender anOffenderWithoutIdentifiersAtRootLevel = Offender.builder()
                .aliases(ImmutableList.of(OffenderAlias.builder()
                        .identifiers(ImmutableList.of(Identifier.builder()
                                .identifierType("PNC")
                                .identifier("71/3344B")
                                .build()))
                        .build())
                ).build();

        Offender anOffenderWithIdentifiersAtRootLevelAndAliases = Offender.builder()
                .identifiers(ImmutableList.of(Identifier.builder()
                        .identifierType("PNC")
                        .identifier("72/5566C")
                        .build()))
                .aliases(ImmutableList.of(OffenderAlias.builder()
                        .identifiers(ImmutableList.of(Identifier.builder()
                                .identifierType("PNC")
                                .identifier("73/7788D")
                                .build()))
                        .build())
                ).build();

        assertThat(XtagTransformer.pncOf(anOffenderWithIdentifiersAtRootLevel)).isEqualTo("70/1122A");
        assertThat(XtagTransformer.pncOf(anOffenderWithoutIdentifiersAtRootLevel)).isEqualTo("71/3344B");
        assertThat(XtagTransformer.pncOf(anOffenderWithIdentifiersAtRootLevelAndAliases)).isEqualTo("72/5566C");
    }
}