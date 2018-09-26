package uk.gov.justice.digital.ndh.service.transforms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.ndh.service.ExceptionLogService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CommonTransformerTest {

    @Test
    public void asListOfWorksProperly() {
        CommonTransformer commonTransformer = new CommonTransformer(mock(XmlMapper.class), new ObjectMapper(), mock(ExceptionLogService.class));

        List<Integer> actual = commonTransformer.asListOf("[1,2,3]");

        final List<Integer> expected = ImmutableList.of(1, 2, 3);

        assertThat(actual).isEqualTo(expected);

        List<String> actualStr = commonTransformer.asListOf("[\"A\",\"B\",\"C\"]");

        final List<String> expectedStr = ImmutableList.of("A","B","C");

        assertThat(actualStr).isEqualTo(expectedStr);


    }

}