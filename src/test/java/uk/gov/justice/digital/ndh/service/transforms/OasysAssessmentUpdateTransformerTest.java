package uk.gov.justice.digital.ndh.service.transforms;

import org.junit.Test;
import uk.gov.justice.digital.ndh.service.MappingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OasysAssessmentUpdateTransformerTest {

    @Test
    public void riskFlagsTransformedCorrectly() {
        OasysAssessmentUpdateTransformer transformer = new OasysAssessmentUpdateTransformer(mock(MappingService.class), mock(CommonTransformer.class));

        assertThat(transformer.deliusRiskFlagsOf(",,,,,")).isEqualTo("L,L,L,L,L,L");
        assertThat(transformer.deliusRiskFlagsOf(",M,H")).isEqualTo("L,M,H");

    }

    @Test
    public void concernFlagsTransformedCorrectly() {

        OasysAssessmentUpdateTransformer transformer = new OasysAssessmentUpdateTransformer(mock(MappingService.class), mock(CommonTransformer.class));

        assertThat(transformer.deliusConcernFlagsOf(",,,,,")).isEqualTo("DK,DK,DK,DK,DK,DK");
        assertThat(transformer.deliusConcernFlagsOf(",YES,NO")).isEqualTo("DK,YES,NO");

    }

}