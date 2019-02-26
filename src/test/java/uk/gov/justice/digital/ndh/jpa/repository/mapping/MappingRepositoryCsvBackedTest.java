package uk.gov.justice.digital.ndh.jpa.repository.mapping;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class MappingRepositoryCsvBackedTest {

    @Test(expected = IllegalStateException.class)
    public void mappingCsvCannotContainDuplicates() throws IOException {
        new MappingRepositoryCsvBacked(new ClassPathResource("mapping_with_duplicates.csv"));
    }

}