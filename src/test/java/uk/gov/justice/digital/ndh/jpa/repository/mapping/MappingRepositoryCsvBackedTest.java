package uk.gov.justice.digital.ndh.jpa.repository.mapping;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingRepositoryCsvBackedTest {

    @Test(expected = IllegalStateException.class)
    public void mappingCsvCannotContainDuplicates() throws IOException {
        new MappingRepositoryCsvBacked(new ClassPathResource("mapping_with_duplicates.csv"));
    }

    @Test
    public void findByCodeTypeAndSourceValueBehavesAppropriately() throws IOException {
        final MappingRepositoryCsvBacked mappingRepositoryCsvBacked = new MappingRepositoryCsvBacked(new ClassPathResource("mapping_code_data.csv"));

        final Optional<MappingCodeData> c6 = mappingRepositoryCsvBacked.findByCodeTypeAndSourceValue(2016L, "C6");
        assertThat(c6.isPresent()).isTrue();
        assertThat(c6.get().getTargetValue()).isEqualTo("C6");
    }

}