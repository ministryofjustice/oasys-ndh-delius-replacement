package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class RequirementLookupRepositoryCsvBackedTest {

    @Test(expected = IllegalStateException.class)
    public void requirementLookupCsvCannotContainDuplicates() throws IOException {
        new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup_with_duplicates.csv"));
    }

}