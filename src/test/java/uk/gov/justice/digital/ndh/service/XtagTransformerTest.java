package uk.gov.justice.digital.ndh.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.justice.digital.ndh.api.nomis.Identifier;
import uk.gov.justice.digital.ndh.api.nomis.Offender;
import uk.gov.justice.digital.ndh.api.nomis.OffenderAlias;
import uk.gov.justice.digital.ndh.service.exception.NDHMappingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


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

    @Test
    public void receptionMovementCodeIsMappedIfPresentInReceptionCodeMapping() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, null, null, mappingService, null,null);

        Mockito.when(mappingService.targetValueOf("C5", 2015L, false)).thenReturn("Jeremy");

        assertThat(xtagTransformer.receptionMovementCodeOf("C5")).isEqualTo("Jeremy");
    }

    @Test
    public void receptionMovementCodeIsMappedToRIfIfNotPresentInReceptionCodeMappingButIsPresentInDischargeMapping() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, null, null, mappingService, null,null);

        Mockito.when(mappingService.targetValueOf("C5", 2015L, false)).thenThrow(NDHMappingException.builder().build());
        Mockito.when(mappingService.targetValueOf("C5", 2016L, false)).thenReturn("Colin");

        assertThat(xtagTransformer.receptionMovementCodeOf("C5")).isEqualTo("R");
    }

    @Test(expected = NDHMappingException.class)
    public void mappingExceptionIsThrownIfReceptionMovementCodeNotMappedInEitherReceptionCodesOrDischargeCodes() {

        final MappingService mappingService = mock(MappingService.class);
        XtagTransformer xtagTransformer = new XtagTransformer(null, null, null, mappingService, null,null);

        Mockito.when(mappingService.targetValueOf("C5", 2015L, false)).thenThrow(NDHMappingException.builder().build());
        Mockito.when(mappingService.targetValueOf("C5", 2016L)).thenThrow(NDHMappingException.builder().build());

        xtagTransformer.receptionMovementCodeOf("C5");
    }

}